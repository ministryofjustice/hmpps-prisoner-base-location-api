package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.UpstreamApiError

@Service
class GetPersonService(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  private val deliusGateway: NDeliusGateway,
) {

  enum class IdentifierType {
    NOMS,
    CRN,
    UNKNOWN,
  }

  fun identifyHmppsId(input: String): IdentifierType {
    val nomsPattern = Regex("^[A-Z]\\d{4}[A-Z]{2}$")
    val crnPattern = Regex("^[A-Z]{1,2}\\d{6}$")

    return when {
      nomsPattern.matches(input) -> IdentifierType.NOMS
      crnPattern.matches(input) -> IdentifierType.CRN
      else -> IdentifierType.UNKNOWN
    }
  }

  /**
   * Returns a Nomis number from a HMPPS ID
   */
  fun getNomisNumber(hmppsId: String): Response<NomisNumber?> {
    when (identifyHmppsId(hmppsId)) {
      IdentifierType.NOMS -> {
        val prisoner = prisonerOffenderSearchGateway.getPrisonOffender(hmppsId)
        if (prisoner.errors.isNotEmpty()) {
          return Response(data = null, errors = prisoner.errors)
        }

        return Response(data = NomisNumber(hmppsId))
      }

      IdentifierType.CRN -> {
        val personFromProbationOffenderSearch = deliusGateway.getPerson(hmppsId)

        if (personFromProbationOffenderSearch.errors.isNotEmpty()) {
          return Response(
            data = null,
            errors = personFromProbationOffenderSearch.errors,
          )
        }

        val nomisNumber = personFromProbationOffenderSearch.data?.identifiers?.nomisNumber ?: return Response(
          data = null,
          errors =
          listOf(
            UpstreamApiError(
              description = "NOMIS number not found",
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              causedBy = UpstreamApi.NDELIUS,
            ),
          ),
        )

        return Response(
          data = NomisNumber(nomisNumber),
        )
      }

      IdentifierType.UNKNOWN ->
        return Response(
          data = null,
          errors =
          listOf(
            UpstreamApiError(
              description = "Invalid HMPPS ID: $hmppsId",
              type = UpstreamApiError.Type.BAD_REQUEST,
              causedBy = UpstreamApi.PRISON_API,
            ),
          ),
        )
    }
  }
}
