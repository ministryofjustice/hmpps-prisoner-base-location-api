package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services

import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.NomisNumber

@Service
class GetPersonService(
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
  fun getNomisNumber(hmppsId: String): Result<NomisNumber> = when (identifyHmppsId(hmppsId)) {
    IdentifierType.NOMS -> {
      Result.success(NomisNumber(hmppsId))
    }
    IdentifierType.CRN -> {
      deliusGateway.getPerson(hmppsId).mapCatching { person ->
        val nomisNumber = person.identifiers.nomisNumber

        if (nomisNumber == null) {
          throw EntityNotFoundException("NOMIS number not found")
        } else {
          NomisNumber(nomisNumber)
        }
      }
    }
    IdentifierType.UNKNOWN ->
      Result.failure(ValidationException("hmppsId is invalid"))
  }
}
