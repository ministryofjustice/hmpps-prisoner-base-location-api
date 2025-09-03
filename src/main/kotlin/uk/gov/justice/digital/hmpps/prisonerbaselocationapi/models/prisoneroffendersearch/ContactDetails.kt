package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch

import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.PhoneNumber

class ContactDetails(
  val phoneNumbers: List<PhoneNumber>?,
  val emailAddresses: List<String>?,
)
