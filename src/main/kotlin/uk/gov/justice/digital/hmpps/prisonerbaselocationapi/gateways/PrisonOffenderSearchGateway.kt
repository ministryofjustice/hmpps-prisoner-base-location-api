package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch.POSPrisoner

@Component
class PrisonOffenderSearchGateway(
  @Value("\${services.prisoner-offender-search.base-url}") baseUrl: String,
) {
  private val webClient = WebClient.builder().baseUrl(baseUrl).build()

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("Prisoner Offender Search")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }

  fun getPrisonOffender(nomsNumber: String): POSPrisoner = webClient
    .get()
    .uri("/prisoner/$nomsNumber")
    .headers({ header -> authenticationHeader().forEach { requestHeader -> header.set(requestHeader.key, requestHeader.value) } })
    .retrieve()
    .bodyToMono(POSPrisoner::class.java)
    .block()!!
}
