package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.controllers.GOALS_RO
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.UpstreamApi
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.io.File

@ExtendWith(HmppsAuthApiExtension::class)
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
abstract class IntegrationTestBase {
  @Autowired
  lateinit var mockMvc: MockMvc

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  final val basePath = "/v1/persons"
  final val defaultCn = "automated-test-client"
  final val invalidNomsId = "G2996UXX"
  final val crn = "AB123123"

  companion object {
    private val nomsId = "G2996UX"
    private val nomsIdFromProbation = "G5555TT"

    val gatewaysFolder = "src/test/kotlin/uk/gov/justice/digital/hmpps/prisonerbaselocationapi/gateways/fixtures"
    private val hmppsAuthMockServer = HmppsAuthMockServer()
    val prisonerOffenderSearchMockServer = ApiMockServer.create(UpstreamApi.PRISONER_OFFENDER_SEARCH)

    @BeforeAll
    @JvmStatic
    fun startMockServers() {
      hmppsAuthMockServer.start()
      hmppsAuthMockServer.stubGetOAuthToken("client", "client-secret")

      prisonerOffenderSearchMockServer.start()
      prisonerOffenderSearchMockServer.stubForGet(
        "/prisoner/$nomsId",
        File(
          "$gatewaysFolder/prisoneroffendersearch/PrisonerByIdResponse.json",
        ).readText(),
      )
      prisonerOffenderSearchMockServer.stubForGet(
        "/prisoner/$nomsIdFromProbation",
        File(
          "$gatewaysFolder/prisoneroffendersearch/PrisonerByIdResponse.json",
        ).readText(),
      )
    }

    @AfterAll
    @JvmStatic
    fun stopMockServers() {
      hmppsAuthMockServer.stop()
      prisonerOffenderSearchMockServer.stop()
    }
  }

  fun getAuthHeader(cn: String = defaultCn): HttpHeaders {
    val headers = HttpHeaders()
    headers.set("subject-distinguished-name", "C=GB,ST=London,L=London,O=Home Office,CN=$cn")
    return headers
  }

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(GOALS_RO),
    scopes: List<String> = listOf("read"),
  ): HttpHeaders = HttpHeaders().apply {
    jwtAuthHelper.setAuthorisationHeader(
      username = username,
      scope = scopes,
      roles = roles,
    )(this)
  }

  fun callApi(path: String): ResultActions = mockMvc.perform(get(path).headers(setAuthorisation()))

  fun getExpectedResponse(filename: String): String = File("./src/test/resources/expected-responses/$filename").readText(Charsets.UTF_8).removeWhitespaceAndNewlines()

  fun callApiWithCN(
    path: String,
    cn: String,
  ): ResultActions = mockMvc.perform(get(path).headers(getAuthHeader(cn)))
}
