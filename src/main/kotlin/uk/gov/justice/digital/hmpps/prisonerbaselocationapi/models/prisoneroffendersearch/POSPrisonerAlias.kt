package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch

import java.time.LocalDate

data class POSPrisonerAlias(
  val firstName: String,
  val lastName: String,
  val middleNames: String? = null,
  var dateOfBirth: LocalDate? = null,
  val gender: String? = null,
  val ethnicity: String? = null,
)
