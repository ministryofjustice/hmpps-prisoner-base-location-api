package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps

import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.roleconfig.ConsumerFilters

data class ConfigAuthorisation(
  val endpoints: List<String>,
  val filters: ConsumerFilters?,
)
