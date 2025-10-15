package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.exceptions

data class ResponseException(
  override var message: String?,
  var statusCode: Int,
  override val cause: Throwable? = null,
) : RuntimeException(message)
