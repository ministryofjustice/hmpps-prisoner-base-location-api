package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch.POSPrisoner

@Service
class GetPrisonerBaseLocationForPersonService(
  @Autowired private val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
) {
  fun getPrisonOffender(nomisNumber: NomisNumber): POSPrisoner = prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber)
}
