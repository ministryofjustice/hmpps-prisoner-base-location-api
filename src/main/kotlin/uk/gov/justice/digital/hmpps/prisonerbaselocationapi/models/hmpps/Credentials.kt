package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps

import java.util.*

data class Credentials(
  val username: String,
  val password: String,
) {
  fun toBasicAuth(): String {
    val encodedCredentials = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
    return "Basic $encodedCredentials"
  }
}
