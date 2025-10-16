package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.extensions

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec
import org.springframework.web.reactive.function.client.WebClientRequestException
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.config.ApiClientConfig
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.exceptions.ResponseException

private val OFFICIAL_SERVER_ERRORS = setOf(502, 503, 504)
private val OFFICIAL_CLIENT_ERRORS = setOf(408)
private val UNOFFICIAL_SERVER_ERRORS = setOf(522, 599)
private val UNOFFICIAL_CLIENT_ERRORS = setOf(499)

val RETRY_ERROR_CODES: Set<Int> = OFFICIAL_SERVER_ERRORS + OFFICIAL_CLIENT_ERRORS + UNOFFICIAL_SERVER_ERRORS + UNOFFICIAL_CLIENT_ERRORS

@Component
class WebClientExtension(
  private val apiClientConfig: ApiClientConfig,
) {
  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  /**
   * This Retry can only be used with idempotent request (e.g. `GET`)
   */
  fun retryForIdempotentRequest(
    uri: String,
    upstream: String,
    statusCodeRetryExhausted: Int = apiClientConfig.statusCodeRetryExhausted,
  ): Retry = apiClientConfig.run {
    Retry.backoff(maxRetryAttempts, minBackOffDuration)
      .filter { it.isSafeToRetry() }
      .onRetryExhaustedThrow { _, retrySignal ->
        throw ResponseException(
          message = "Failed to process after ${retrySignal.totalRetries()} retries",
          statusCode = statusCodeRetryExhausted,
          uri = uri,
          upstream = upstream,
          cause = retrySignal.failure().cause,
        )
      }
  }.doBeforeRetry { log.debug("WebClient Retry #{} for failure {}", it.totalRetries(), it.failure().message) }

  private fun Throwable.isSafeToRetry() = when (this) {
    is WebClientRequestException, is ResponseException -> true
    else -> false
  }
}

fun ResponseSpec.onServerErrorTerminate(
  statusPredicate: ((HttpStatusCode) -> Boolean) = defaultStatusPredicate,
): ResponseSpec = this.onStatus(statusPredicate) { response ->
  Mono.error(ResponseException(null, response.statusCode().value()))
}

private val defaultStatusPredicate: ((HttpStatusCode) -> Boolean) = { status -> RETRY_ERROR_CODES.contains(status.value()) }
