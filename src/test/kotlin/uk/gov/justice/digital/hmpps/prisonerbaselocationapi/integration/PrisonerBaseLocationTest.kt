package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration

import com.github.tomakehurst.wiremock.client.WireMock.exactly
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration.wiremock.PrisonOffenderSearchApiExtension.Companion.prisonOffenderSearch

class PrisonerBaseLocationTest : IntegrationTestBase() {

  @Nested
  @DisplayName("GET /v1/persons/{prisonNumber}/prisoner-base-location")
  inner class GetPrisonerBaseLocation {
    private val apiPath = "/v1/persons/{prisonNumber}/prisoner-base-location"
    private val upstreamApiPathPattern = urlPathMatching("/prisoner/([A-Za-z0-9])+")

    val validPrisonNumber = "A1234AA"

    @Nested
    @DisplayName("Valid prison number")
    inner class GivenAValidPrisonNumber {
      private val prisonNumber = validPrisonNumber

      @BeforeEach
      internal fun setUp() = hmppsAuth.stubGrantToken()

      @Test
      fun `prisoner info found - return their base location`() {
        prisonOffenderSearch.stubGetPrisonOffender()

        requestApi(prisonNumber)
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.inPrison").isEqualTo(true)
          .jsonPath("$.prisonId").isEqualTo("MDI")
          .jsonPath("$.lastPrisonId").isEqualTo("MDI")
          .jsonPath("$.lastMovementType").isEqualTo("Admission")
          .jsonPath("$.receptionDate").isEqualTo("2020-01-01")

        verifyUpstreamApi()
      }

      @Test
      fun `prisoner info not found - return 404 not found`() {
        prisonOffenderSearch.stubUpstreamError(HttpStatus.NOT_FOUND)

        requestApi(prisonNumber)
          .expectStatus().isNotFound
      }

      @Test
      fun `prison offender search api error - return 5xx server error`() {
        prisonOffenderSearch.stubUpstreamError()

        requestApi(prisonNumber)
          .expectStatus().isEqualTo(599)
      }
    }

    @Nested
    @DisplayName("Given some upstream errors")
    inner class GivenUpstreamErrors {
      private val prisonNumber = validPrisonNumber

      // first request + X retries
      private val maxApiAttempts = 1 + maxRetryAttempts

      @BeforeEach
      internal fun setUp() = hmppsAuth.stubGrantToken()

      @Nested
      @DisplayName("And these are error responses")
      inner class AndErrorResponses {
        @ParameterizedTest
        @ValueSource(ints = [502, 503, 504, 408, 522, 599, 499])
        fun `retry until exhausted, given selected error`(errorCode: Int) {
          prisonOffenderSearch.stubUpstreamError(errorCode)

          requestApi(prisonNumber)
            .expectStatus().isEqualTo(599)
          verifyUpstreamApi(maxApiAttempts)
        }

        @Test
        fun `retry and succeed before last attempt`() {
          val expectedAttempts = maxApiAttempts - 1
          prisonOffenderSearch.stubGetPrisonOffenderRetry(
            scenario = "Retry error response, succeed in middle",
            numberOfRequests = expectedAttempts,
          )

          requestApi(prisonNumber)
            .expectStatus().isOk
            .expectBody().notEmpty()
          verifyUpstreamApi(expectedAttempts)
        }

        @Test
        fun `retry and succeed at last attempt`() {
          prisonOffenderSearch.stubGetPrisonOffenderRetry(scenario = "Retry error response, succeed at last")

          requestApi(prisonNumber)
            .expectStatus().isOk
            .expectBody().notEmpty()
          verifyUpstreamApi(maxApiAttempts)
        }
      }

      @Nested
      @DisplayName("And these are connection errors")
      inner class AndConnectionErrors {
        @Test
        fun `retry and succeed at last, given earlier connection reset by peer (RST)`() {
          prisonOffenderSearch.stubUpstreamConnectionResetError(scenario = "Retry after RST error, succeed at last")

          requestApi(prisonNumber)
            .expectStatus().isOk
            .expectBody().notEmpty()
          verifyUpstreamApi(maxApiAttempts)
        }

        @Test
        fun `retry and succeed at last, given earlier connection timed out`() {
          prisonOffenderSearch.stubUpstreamConnectionTimedOutError(scenario = "Retry after response timed out, succeed at last")

          requestApi(prisonNumber)
            .expectStatus().isOk
            .expectBody().notEmpty()
          verifyUpstreamApi(maxApiAttempts)
        }
      }
    }

    @Test
    fun `No auth token - return 401 unauthorized`() {
      hmppsAuth.stubGrantToken()
      prisonOffenderSearch.stubGetPrisonOffender()

      requestApi(validPrisonNumber, authorised = false)
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Auth token does not contain correct role - return 403 forbidden`() {
      hmppsAuth.stubGrantToken()
      prisonOffenderSearch.stubGetPrisonOffender()

      requestApi(validPrisonNumber, "SOME_OTHER_ROLE")
        .expectStatus().isForbidden
    }

    @Test
    fun `Invalid prison number - return 404 not found`() {
      val invalidPrisonNumber = "XXX"

      requestApi(invalidPrisonNumber)
        .expectStatus().isNotFound
    }

    @Test
    fun `Hmpps Auth errors - return 502 bad gateway`() {
      hmppsAuth.stubServiceUnavailable()
      prisonOffenderSearch.stubGetPrisonOffender()

      requestApi(validPrisonNumber)
        .expectStatus().is5xxServerError
    }

    private fun requestApi(
      prisonNumber: String,
      apiClientRole: String = "ROLE_PRISONER_BASE_LOCATION__LOCATIONS_RO",
      authorised: Boolean = true,
    ): WebTestClient.ResponseSpec = webTestClient.get()
      .uri(apiPath, prisonNumber)
      .also { if (authorised) it.headers(setAuthorisation(apiClientRole)) }
      .exchange()

    private fun verifyUpstreamApi(expectedCount: Int = 1) = prisonOffenderSearch.verify(exactly(expectedCount), getRequestedFor(upstreamApiPathPattern))
  }
}
