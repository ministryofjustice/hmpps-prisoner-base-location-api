package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.gateways.PrisonOffenderSearchGateway
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch.POSPrisoner

@Service
class GetPrisonOffenderService(
  @Autowired private val prisonOffenderSearchGateway: PrisonOffenderSearchGateway,
) {
  fun getPrisonOffender(nomisNumber: String): POSPrisoner = prisonOffenderSearchGateway.getPrisonOffender(nomisNumber)
}
