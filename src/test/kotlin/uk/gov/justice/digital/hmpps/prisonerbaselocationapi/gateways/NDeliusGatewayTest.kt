package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.gateways

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.core.codec.DecodingException
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.NomisNumber

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NDeliusGateway::class],
)
class NDeliusGatewayTest(
  @MockitoBean private val hmppsAuthGateway: HmppsAuthGateway,
  @Autowired private val nDeliusGateway: NDeliusGateway,
) : DescribeSpec({
  val server = WireMockServer(wireMockConfig().port(4003))
  val crnNumber = "X123456"

  whenever(hmppsAuthGateway.getClientToken("Prisoner Offender Search")).thenReturn("mock-bearer-token")

  beforeTest {
    server.start()
    configureFor("localhost", server.port())
  }

  afterTest {
    server.stop()
  }

  fun respondWith(status: Int, body: String = "") {
    stubFor(
      get(urlEqualTo("/search/probation-cases"))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withHeader("Content-Type", "application/json")
            .withBody(body),
        ),
    )
  }

  describe("getNomisNumber") {
    it("should return a nomis number if one is returned") {
      respondWith(200, """{"firstName":"John", "surname":"Doe", "otherIds": {"nomsNumber": "123456"}}""")

      val result = nDeliusGateway.getNomisNumber(crnNumber)

      result shouldBe NomisNumber("123456")
    }
    it("should not return a nomis number if one is not returned") {
      respondWith(200, """{"firstName":"John", "surname":"Doe"}""")

      val result = nDeliusGateway.getNomisNumber(crnNumber)

      result shouldBe null
    }
    it("should not catch any web client response exceptions") {
      respondWith(500)

      shouldThrow<WebClientResponseException.InternalServerError> {
        nDeliusGateway.getNomisNumber(crnNumber)
      }
    }
    it("should not catch any decoding exceptions") {
      respondWith(200, """{"firstName":"John"}""")

      shouldThrow<DecodingException> {
        nDeliusGateway.getNomisNumber(crnNumber)
      }
    }
  }
})
