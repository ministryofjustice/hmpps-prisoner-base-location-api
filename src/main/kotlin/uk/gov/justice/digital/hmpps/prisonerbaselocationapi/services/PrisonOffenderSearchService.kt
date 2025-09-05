package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch.POSPrisoner

@Service
class PrisonOffenderSearchService(
  private val prisonOffenderSearchWebClient: WebClient,
) {
  fun getPrisonOffender(nomisNumber: String): POSPrisoner = prisonOffenderSearchWebClient.get()
    .uri("/prisoner/{nomisNumber}", nomisNumber)
    .retrieve()
    .bodyToMono(POSPrisoner::class.java)
    .block()!!
}
