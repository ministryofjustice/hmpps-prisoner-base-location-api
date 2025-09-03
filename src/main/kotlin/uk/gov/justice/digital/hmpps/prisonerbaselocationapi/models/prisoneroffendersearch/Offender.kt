package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch

import java.time.LocalDate
import kotlin.collections.List
import kotlin.collections.listOf

data class Offender(
  val firstName: String,
  val surname: String,
  val middleNames: List<String> = listOf(),
  val dateOfBirth: LocalDate? = null,
  val gender: String? = null,
  val offenderProfile: OffenderProfile = OffenderProfile(),
  val offenderAliases: List<OffenderAlias> = listOf(),
  val contactDetails: ContactDetails? = null,
  val otherIds: OtherIds = OtherIds(),
  val age: Number = 0,
  val activeProbationManagedSentence: Boolean = false,
  val currentRestriction: Boolean = false,
  val restrictionMessage: String? = null,
  val currentExclusion: Boolean = false,
  val exclusionMessage: String? = null,
) {
  fun getNomisNumber(): String? = otherIds.nomsNumber
}
