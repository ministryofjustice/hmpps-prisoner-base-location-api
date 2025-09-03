package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch

import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.LastMovementType
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.PrisonerBaseLocation
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.let

data class POSPrisoner(
  val prisonerNumber: String? = null,
  val pncNumber: String? = null,
  val croNumber: String? = null,
  val bookingId: String? = null,
  val firstName: String,
  val middleNames: String? = null,
  val lastName: String,
  val dateOfBirth: LocalDate? = null,
  val gender: String? = null,
  val ethnicity: String? = null,
  val youthOffender: Boolean,
  val maritalStatus: String? = null,
  val smoker: String? = null,
  val status: String? = null,
  val lastMovementTypeCode: String? = null,
  val inOutStatus: String? = null,
  val prisonId: String? = null,
  val lastPrisonId: String? = null,
  val prisonName: String? = null,
  val cellLocation: String? = null,
  val aliases: List<POSPrisonerAlias> = listOf(),
  val csra: String? = null,
  val category: String? = null,
  val receptionDate: String? = null,
  val heightCentimetres: Int? = null,
  val weightKilograms: Int? = null,
  val hairColour: String? = null,
  val rightEyeColour: String? = null,
  val leftEyeColour: String? = null,
  val facialHair: String? = null,
  val shapeOfFace: String? = null,
  val build: String? = null,
  val shoeSize: Int? = null,
  val tattoos: List<POSBodyMark>? = null,
  val scars: List<POSBodyMark>? = null,
  val marks: List<POSBodyMark>? = null,
  val personalCareNeeds: List<POSPersonalCareNeed>? = null,
  val languages: List<POSLanguage>? = null,
  val identifiers: List<POSIdentifier>? = null,
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
