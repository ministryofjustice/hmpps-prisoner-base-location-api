package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.http.HttpStatus

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

class PrisonOffenderSearchMockServer : WireMockServer(4000) {
  fun stubGetPrisonOffender() {
    stubFor(
      get(urlPathMatching("/prisoner/([A-Za-z0-9])*"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """
                {
                  "firstName": "John",
                  "lastName": "Doe"
                }
              """.trimIndent(),
            ),
        ),
    )
  }
  fun stubUpstreamError(httpStatus: HttpStatus = HttpStatus.GATEWAY_TIMEOUT) {
    stubFor(
      get(urlPathMatching("/prisoner/([A-Za-z0-9])*"))
        .willReturn(
          aResponse()
            .withStatus(httpStatus.value()),
        ),
    )
  }
}
