package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class NDeliusApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val nDelius = NDeliusMockServer()
  }

  override fun beforeAll(context: ExtensionContext): Unit = nDelius.start()
  override fun beforeEach(context: ExtensionContext): Unit = nDelius.resetAll()
  override fun afterAll(context: ExtensionContext): Unit = nDelius.stop()
}

class NDeliusMockServer : WireMockServer(4003) {
  fun stubPrisonerWithNomsNumber() {
    stubFor(
      get(urlEqualTo("/search/probation-cases"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """
                {
                  "firstName": "John",
                  "surname": "Doe",
                  "otherIds": {
                    "nomsNumber": "A1234AA"
                  }
                }
              """.trimIndent(),
            ),
        ),
    )
  }
  fun stubPrisonerWithoutNomsNumber() {
    stubFor(
      get(urlEqualTo("/search/probation-cases"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """
                {
                  "firstName": "John",
                  "surname": "Doe"
                }
              """.trimIndent(),
            ),
        ),
    )
  }
}
