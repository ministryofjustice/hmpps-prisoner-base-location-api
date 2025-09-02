package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch.Offender

@Component
class NDeliusGateway(
  @Value("\${services.ndelius.base-url}") baseUrl: String,
) {
  private val webClient = WebClient.builder().baseUrl(baseUrl).build()

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("nDelius")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }

  fun getNomisNumber(crnNumber: String): NomisNumber? {
    val headers = authenticationHeader() + mapOf("crn" to crnNumber)
    val offender = webClient
      .get()
      .uri("/search/probation-cases")
      .headers({ header -> headers.forEach { requestHeader -> header.set(requestHeader.key, requestHeader.value) } })
      .retrieve()
      .bodyToMono(Offender::class.java)
      .block()!!

    return offender.getNomisNumber()?.let { NomisNumber(it) }
  }
}
