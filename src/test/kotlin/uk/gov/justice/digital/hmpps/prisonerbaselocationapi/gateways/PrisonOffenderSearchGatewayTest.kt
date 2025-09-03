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
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch.POSPrisoner

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonOffenderSearchGateway::class],
)
class PrisonOffenderSearchGatewayTest(
  @MockitoBean private val hmppsAuthGateway: HmppsAuthGateway,
  @Autowired private val prisonOffenderSearchGateway: PrisonOffenderSearchGateway,
) : DescribeSpec({
  val server = WireMockServer(wireMockConfig().port(4000))
  val nomsNumber = NomisNumber("123456")

  whenever(hmppsAuthGateway.getClientToken("Prisoner Offender Search")).thenReturn("mock-bearer-token")

  beforeTest {
    server.start()
    configureFor("localhost", server.port())
  }

  fun respondWith(status: Int, body: String = "") {
    stubFor(
      get(urlEqualTo("/prisoner/${nomsNumber.nomisNumber}"))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withHeader("Content-Type", "application/json")
            .withBody(body),
        ),
    )
  }

  describe("getPrisonOffender") {
    it("should return a success response") {
      respondWith(200, """{"firstName":"John", "lastName":"Doe"}""")

      val result = prisonOffenderSearchGateway.getPrisonOffender(nomsNumber)

      result shouldBe POSPrisoner(firstName = "John", lastName = "Doe", youthOffender = false)
    }
    it("should not catch any web client response exceptions") {
      respondWith(404)

      shouldThrow<WebClientResponseException.NotFound> {
        prisonOffenderSearchGateway.getPrisonOffender(nomsNumber)
      }
    }
    it("should not catch any decoding exceptions") {
      respondWith(200, """{"firstName":"John"}""")

      shouldThrow<DecodingException> {
        prisonOffenderSearchGateway.getPrisonOffender(nomsNumber)
      }
    }
  }
})
