package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class ContactDetailsWithEmailAndPhone(
  val phoneNumbers: List<PhoneNumber>?,
  @Schema(description = "A list of email addresses", example = "leslie.knope@example.com")
  val emails: List<String>?,
)
