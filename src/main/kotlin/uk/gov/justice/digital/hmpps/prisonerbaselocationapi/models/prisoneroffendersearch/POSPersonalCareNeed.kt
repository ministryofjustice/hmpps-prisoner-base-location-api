package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch

data class POSPersonalCareNeed(
  val problemType: String?,
  val problemCode: String?,
  val problemStatus: String?,
  val problemDescription: String?,
  val commentText: String?,
  val startDate: String?,
  val endDate: String?,
)
