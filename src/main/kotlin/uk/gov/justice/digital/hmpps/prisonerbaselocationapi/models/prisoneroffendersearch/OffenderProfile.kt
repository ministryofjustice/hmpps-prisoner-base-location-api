package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch

import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.Disability

data class OffenderProfile(
  val ethnicity: String? = null,
  val nationality: String? = null,
  val religion: String? = null,
  val sexualOrientation: String? = null,
  val disabilities: List<Disability> = emptyList(),
)
