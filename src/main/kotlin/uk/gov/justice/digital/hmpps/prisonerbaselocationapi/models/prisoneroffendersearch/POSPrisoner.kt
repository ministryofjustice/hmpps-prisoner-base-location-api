package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch

import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.LastMovementType
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.PrisonerBaseLocation
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.String
import kotlin.let

data class POSPrisoner(
  val firstName: String,
  val lastName: String,
  val inOutStatus: String? = null,
  val prisonId: String? = null,
  val lastPrisonId: String? = null,
  val lastMovementTypeCode: String? = null,
  val receptionDate: String? = null,
) {
  fun toBaseLocation(): PrisonerBaseLocation {
    val inPrison = this.inOutStatus == "IN"

    return PrisonerBaseLocation(
      inPrison = inPrison,
      prisonId = if (inPrison) this.prisonId else null,
      lastPrisonId = this.lastPrisonId,
      lastMovementType = this.lastMovementTypeCode?.let { lastMovementTypeCode ->
        when (lastMovementTypeCode) {
          "ADM" -> LastMovementType.ADMISSION
          "REL" -> LastMovementType.RELEASE
          "TRN" -> LastMovementType.TRANSFERS
          "CRT" -> LastMovementType.COURT
          "TAP" -> LastMovementType.TEMPORARY_ABSENCE
          else -> null
        }
      },
      receptionDate = this.receptionDate?.let { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) },
    )
  }
}
