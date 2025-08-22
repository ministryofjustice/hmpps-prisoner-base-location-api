package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.mockservers

import com.atlassian.oai.validator.wiremock.OpenApiValidationListener
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.UpstreamApi

class ApiMockServer(
  config: WireMockConfiguration,
  private val validationListener: OpenApiValidationListener? = null,
) : WireMockServer(config) {
  companion object {
    // These ports must match the config in the yaml files
    fun create(upstreamApi: UpstreamApi): ApiMockServer {
      val apiMockerServerConfig =
        when (upstreamApi) {
          UpstreamApi.PRISONER_OFFENDER_SEARCH -> ApiMockServerConfig(4000, "prisoner-search.json")
          // USE PRISM
          UpstreamApi.NDELIUS -> ApiMockServerConfig(4003)
          UpstreamApi.PRISON_API -> ApiMockServerConfig(4000)
          UpstreamApi.TEST -> ApiMockServerConfig(4005, "test.json")
        }

      val wireMockConfig = WireMockConfiguration.wireMockConfig().port(apiMockerServerConfig.port)

      if (apiMockerServerConfig.configPath != null) {
        val specPath = "src/test/resources/openapi-specs/${apiMockerServerConfig.configPath}"
        val validationListener = OpenApiValidationListener(specPath)
        return ApiMockServer(
          wireMockConfig.extensions(ResetValidationEventListener(validationListener)),
          validationListener,
        )
      }

      return ApiMockServer(wireMockConfig)
    }
  }

  init {
    if (validationListener != null) {
      super.addMockServiceRequestListener(validationListener)
    }
  }

  fun resetValidator() {
    this.validationListener?.reset()
  }

  fun assertValidationPassed() {
    this.validationListener?.assertValidationPassed()
  }

  fun stubForGet(
    path: String,
    body: String,
    status: HttpStatus = HttpStatus.OK,
  ) {
    stubFor(
      get(path)
        .withHeader(
          "Authorization",
          matching("Bearer ${HmppsAuthMockServer.TOKEN}"),
        ).willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(status.value())
            .withBody(body.trimIndent()),
        ),
    )
  }
}
