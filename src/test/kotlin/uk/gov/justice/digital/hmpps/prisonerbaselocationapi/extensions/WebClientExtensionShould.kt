package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.extensions

import com.github.tomakehurst.wiremock.client.WireMock.exactly
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.matching.UrlPattern
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import reactor.util.retry.Retry
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.config.ApiClientConfig
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.mockservers.ApiMockServerExtension
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.mockservers.ApiMockServerExtension.Companion.apiMockServer
import java.time.Duration

@ExtendWith(ApiMockServerExtension::class)
class WebClientExtensionShould {
  private val webClient: WebClient = TestWebClient(apiMockServer.baseUrl()).client
  private val webClientExtension = ApiClientConfig(
    healthTimeout = Duration.ofMillis(10),
    responseTimeout = Duration.ofMillis(20),
    maxRetryAttempts = 2,
    minBackOffDuration = Duration.ofMillis(5),
    statusCodeRetryExhausted = 599,
  ).let { apiClientConfig -> WebClientExtension(apiClientConfig) }

  @Nested
  @DisplayName("Retry upstream's idempotent request when necessary")
  inner class RetryUpstreamIdempotentRequest {
    private val upstream = "TestApi"
    private val body = """{"success": true}"""

    @Nested
    @DisplayName("Given successful response from upstream")
    inner class GivenResponseFromUpstream {
      private val getPath = "/path"
      private val getPathById = "/path/{id}"
      private val getPathByIdPattern = urlPathMatching("/path/([A-Za-z0-9])+")

      @Test
      fun `receive response from upstream GET, without retry`() {
        apiMockServer.stubForGet(getPath, body)
        val result = getRequestWithRetry(uri = getPath)
        assertTrue(result.success)
        verifyApiGetPath(url = getPath)
      }

      @Test
      fun `receive response from upstream GET with argument, without retry`() {
        apiMockServer.stubForGet(getPathByIdPattern, body)
        val result = getRequestWithRetry(getPathById, "ID123")
        assertTrue(result.success)
        verifyApiGetPathById(urlPattern = getPathByIdPattern)
      }
    }

    @Nested
    @DisplayName("Given an error response from upstream")
    inner class GivenErrorResponseReceived {
      private val getPath2 = "/path2"
      private val getPath2ById = "/path2/{id}"
      private val getPath2ByIdPattern = urlPathMatching("/path2/([A-Za-z0-9])+")

      @ParameterizedTest
      @ValueSource(ints = [502, 503, 504, 408, 522, 599, 499])
      fun `retry idempotent request of GET`(statusCode: Int) {
        statusCode.let { apiMockServer.stubForRetryGet("$it", getPath2, 3, it, 200, body) }
        val result = getRequestWithRetry(getPath2)
        assertTrue(result.success)
        verifyApiGetPath(url = getPath2, expectedCount = 3)
      }

      @ParameterizedTest
      @ValueSource(ints = [502, 503, 504, 408, 522, 599, 499])
      fun `retry idempotent request of GET with argument`(statusCode: Int) {
        statusCode.let { apiMockServer.stubForRetryGet("$it", getPath2ByIdPattern, 3, it, 200, body) }
        val result = getRequestWithRetry(getPath2ById, "ID456")
        assertTrue(result.success)
        verifyApiGetPathById(urlPattern = getPath2ByIdPattern, expectedCount = 3)
      }
    }

    private fun verifyApiGetPath(url: String, expectedCount: Int = 1) = apiMockServer.verify(exactly(expectedCount), getRequestedFor(urlPathEqualTo(url)))
    private fun verifyApiGetPathById(urlPattern: UrlPattern, expectedCount: Int = 1) = apiMockServer.verify(exactly(expectedCount), getRequestedFor(urlPattern))

    private fun getRequestWithRetry(uri: String) = requestWithRetry(webClient.get().uri(uri), retrySpec(uri))
    private fun getRequestWithRetry(uri: String, vararg uriVariables: Any) = requestWithRetry(webClient.get().uri(uri, *uriVariables), retrySpec(uri))

    private fun retrySpec(uri: String) = webClientExtension.retryForIdempotentRequest(uri, upstream)
    private fun <S : RequestHeadersSpec<S>> requestWithRetry(
      requestSpec: RequestHeadersSpec<S>,
      retrySpec: Retry,
    ): TestApiResult = requestSpec
      .header("Authorization", apiMockServer.authHeader)
      .retrieve()
      .onServerErrorTerminate()
      .bodyToMono(TestApiResult::class.java)
      .retryWhen(retrySpec)
      .block()!!
  }
}

data class TestApiResult(
  val success: Boolean,
)
