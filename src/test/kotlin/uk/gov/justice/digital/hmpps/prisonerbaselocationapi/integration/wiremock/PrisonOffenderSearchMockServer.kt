package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.http.Fault
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.mockservers.ApiMockServer
import kotlin.Int

class PrisonOffenderSearchApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val prisonOffenderSearch = PrisonOffenderSearchMockServer()
  }

  override fun beforeAll(context: ExtensionContext): Unit = prisonOffenderSearch.start()
  override fun beforeEach(context: ExtensionContext): Unit = prisonOffenderSearch.resetAll()
  override fun afterAll(context: ExtensionContext): Unit = prisonOffenderSearch.stop()
}

class PrisonOffenderSearchMockServer : ApiMockServer(4000) {
  private val getPrisonerByIdUrlPathPattern = urlPathMatching(getPrisonerByIdUrlPattern)

  companion object {
    val getPrisonerByIdUrlPattern = "/prisoner/([A-Za-z0-9])+(\\?.+)*"
    val getPrisonerByIdBody = """
    {
        "firstName": "SOME",
        "lastName": "ONE",
        "lastMovementTypeCode": "ADM",
        "inOutStatus": "IN",
        "prisonId": "MDI",
        "lastPrisonId": "MDI",
        "receptionDate": "2020-01-01"
    }
    """.trimIndent().replace("\n", "")
  }

  fun stubGetPrisonOffender() = stubForGet(
    pathPattern = getPrisonerByIdUrlPathPattern,
    body = getPrisonerByIdBody,
  )

  fun stubUpstreamError(httpStatus: HttpStatus = HttpStatus.GATEWAY_TIMEOUT) = stubUpstreamError(httpStatus.value())

  fun stubUpstreamError(httpStatusCode: Int) = stubForGet(
    pathPattern = getPrisonerByIdUrlPathPattern,
    status = httpStatusCode,
  )

  fun stubGetPrisonOffenderRetry(
    scenario: String,
    numberOfRequests: Int = 1 + IntegrationTestBase.maxRetryAttempts,
    failedStatus: Int = 499,
    endStatus: Int = HttpStatus.OK.value(),
  ) = stubForRetryGet(
    scenario = scenario,
    pathPattern = getPrisonerByIdUrlPathPattern,
    numberOfRequests = numberOfRequests,
    failedStatus = failedStatus,
    endStatus = endStatus,
    body = getPrisonerByIdBody,
  )

  fun stubUpstreamConnectionResetError(
    scenario: String,
    numberOfRequests: Int = 1 + IntegrationTestBase.maxRetryAttempts,
    endStatus: Int = HttpStatus.OK.value(),
  ) = stubForRetryGetWithFault(
    scenario = scenario,
    pathPattern = getPrisonerByIdUrlPathPattern,
    numberOfRequests = numberOfRequests,
    fault = Fault.CONNECTION_RESET_BY_PEER,
    endStatus = endStatus,
    body = getPrisonerByIdBody,
  )

  fun stubUpstreamConnectionTimedOutError(
    scenario: String,
    numberOfRequests: Int = 1 + IntegrationTestBase.maxRetryAttempts,
    endStatus: Int = HttpStatus.OK.value(),
  ) = stubForRetryGetWithDelays(
    scenario = scenario,
    pathPattern = getPrisonerByIdUrlPathPattern,
    numberOfRequests = numberOfRequests,
    delayMs = IntegrationTestBase.apiClientTimeoutMs + 1,
    endStatus = endStatus,
    body = getPrisonerByIdBody,
  )
}
