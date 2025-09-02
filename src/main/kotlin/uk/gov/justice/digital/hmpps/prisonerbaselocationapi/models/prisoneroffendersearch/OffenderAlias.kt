package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch

import java.time.LocalDate

data class OffenderAlias(
  val firstName: String,
  val surname: String,
  val middleNames: List<String> = listOf(),
  var dateOfBirth: LocalDate? = null,
  val gender: String? = null,
)
