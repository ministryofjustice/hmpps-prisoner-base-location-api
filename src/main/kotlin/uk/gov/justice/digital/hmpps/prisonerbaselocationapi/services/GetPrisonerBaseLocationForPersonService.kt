package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.PrisonerBaseLocation

@Service
class GetPrisonerBaseLocationForPersonService(
  @Autowired private val getPersonService: GetPersonService,
  @Autowired private val prisonerBaseLocationProvider: PrisonerBaseLocationProvider,
) {
  fun execute(
    hmppsId: String,
  ): Result<PrisonerBaseLocation> = getPersonService.getNomisNumber(hmppsId).mapCatching {
    prisonerBaseLocationProvider.getBaseLocation(it).getOrThrow()
  }
}
