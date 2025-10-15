package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.extensions.WebClientExtension
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.extensions.onServerErrorTerminate
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch.POSPrisoner

@Service
class PrisonOffenderSearchService(
  private val prisonOffenderSearchWebClient: WebClient,
  private val webClientExtension: WebClientExtension,
) {
  fun getPrisonOffender(nomisNumber: String): POSPrisoner = prisonOffenderSearchWebClient.get()
    .uri("/prisoner/{nomisNumber}", nomisNumber)
    .retrieve()
    .onServerErrorTerminate()
    .bodyToMono(POSPrisoner::class.java)
    .retryWhen(webClientExtension.retryForIdempotentRequest())
    .block()!!
}
