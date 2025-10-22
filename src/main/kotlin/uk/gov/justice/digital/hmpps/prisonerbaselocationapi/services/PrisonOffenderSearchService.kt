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
  fun getPrisonOffender(prisonNumber: String): POSPrisoner = "/prisoner/{prisonNumber}?$GET_PRISONER_PARAMS".let { uri ->
    prisonOffenderSearchWebClient.get()
      .uri(uri, prisonNumber)
      .retrieve()
      .onServerErrorTerminate()
      .bodyToMono(POSPrisoner::class.java)
      .retryWhen(webClientExtension.retryForIdempotentRequest(uri, "Prisoner Search"))
      .block()!!
  }
}

private const val GET_PRISONER_PARAMS = "responseFields=firstName,lastName,inOutStatus,prisonId,lastPrisonId,lastMovementTypeCode,receptionDate"
