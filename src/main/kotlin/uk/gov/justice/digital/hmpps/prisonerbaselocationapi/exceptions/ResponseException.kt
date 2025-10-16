package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.exceptions

data class ResponseException(
  override val message: String?,
  val statusCode: Int,
  val uri: String? = null,
  val upstream: String? = null,
  override val cause: Throwable? = null,
) : RuntimeException(message)
