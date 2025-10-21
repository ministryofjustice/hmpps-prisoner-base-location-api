package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.extensions

import com.github.tomakehurst.wiremock.client.WireMock.exactly
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.matching.UrlPattern
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import reactor.util.retry.Retry
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.config.ApiClientConfig
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.exceptions.ResponseException
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.mockservers.ApiMockServerExtension
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.mockservers.ApiMockServerExtension.Companion.apiMockServer
import java.time.Duration

@ExtendWith(ApiMockServerExtension::class)
class WebClientExtensionShould {
  private val webClient: WebClient = TestWebClient(apiMockServer.baseUrl(), connectTimeoutMillis = 15, responseTimeoutMillis = 20).client
  private val unreachableWebClient: WebClient = TestWebClient(baseUrl = "http://10.255.255.1:81", connectTimeoutMillis = 1, responseTimeoutMillis = 1).client

  private val webClientExtension = ApiClientConfig(
    healthTimeout = Duration.ofMillis(10),
    timeout = Duration.ofMillis(20),
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
        statusCode.let { apiMockServer.stubForRetryGet("Retry $it", getPath2, 3, it, 200, body) }
        val result = getRequestWithRetry(getPath2)
        assertTrue(result.success)
        verifyApiGetPath(url = getPath2, expectedCount = 3)
      }

      @ParameterizedTest
      @ValueSource(ints = [502, 503, 504, 408, 522, 599, 499])
      fun `retry idempotent request of GET with argument`(statusCode: Int) {
        statusCode.let { apiMockServer.stubForRetryGet("Retry $it", getPath2ByIdPattern, 3, it, 200, body) }
        val result = getRequestWithRetry(getPath2ById, "ID456")
        assertTrue(result.success)
        verifyApiGetPathById(urlPattern = getPath2ByIdPattern, expectedCount = 3)
      }
    }

    @Nested
    @DisplayName("Given no response from upstream")
    inner class GivenNoResponseFromUpstream {
      private val getPath3 = "/path3"

      @Test
      fun `retry idempotent request of GET, after connection reset by peer (RST)`() {
        apiMockServer.stubForRetryGetWithFault("Retry RST", getPath3, 2, Fault.CONNECTION_RESET_BY_PEER, 200, body)
        val result = getRequestWithRetry(getPath3)
        assertTrue(result.success)
        verifyApiGetPath(url = getPath3, expectedCount = 2)
      }

      @Test
      fun `retry idempotent request of GET, after response timed out`() {
        apiMockServer.stubForRetryGetWithDelays("Retry response timed out", getPath3, 2, 20, 200, body)
        val result = getRequestWithRetry(getPath3)
        assertTrue(result.success)
        verifyApiGetPath(url = getPath3, expectedCount = 2)
      }

      @Test
      fun `retry idempotent request of GET, after connection timed out`() {
        assertThrows<ResponseException> { getRequestWithRetry(getPath3, client = unreachableWebClient) }
      }
    }

    private fun verifyApiGetPath(url: String, expectedCount: Int = 1) = apiMockServer.verify(exactly(expectedCount), getRequestedFor(urlPathEqualTo(url)))
    private fun verifyApiGetPathById(urlPattern: UrlPattern, expectedCount: Int = 1) = apiMockServer.verify(exactly(expectedCount), getRequestedFor(urlPattern))

    private fun getRequestWithRetry(uri: String, client: WebClient = webClient) = requestWithRetry(client.get().uri(uri), retrySpec(uri))
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
