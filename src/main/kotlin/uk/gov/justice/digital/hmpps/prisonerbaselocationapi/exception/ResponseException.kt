package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.exception

data class ResponseException(
  override var message: String?,
  var statusCode: Int,
) : RuntimeException(message)
