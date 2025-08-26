package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.mockservers

data class ApiMockServerConfig(
  val port: Int,
  val configPath: String? = null,
)
