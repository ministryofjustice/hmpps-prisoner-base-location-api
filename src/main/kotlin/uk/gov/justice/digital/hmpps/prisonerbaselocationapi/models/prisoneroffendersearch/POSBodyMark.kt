package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch

import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.BodyMark

data class POSBodyMark(
  val bodyPart: String?,
  val comment: String?,
) {
  fun toBodyMark() = BodyMark(bodyPart = this.bodyPart, comment = this.comment)
}
