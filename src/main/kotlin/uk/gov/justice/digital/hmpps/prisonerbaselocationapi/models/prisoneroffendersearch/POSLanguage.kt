package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch

data class POSLanguage(
  val type: String?,
  val code: String?,
  val readSkill: String?,
  val writeSkill: String?,
  val speakSkill: String?,
  val interpreterRequested: Boolean?,
)
