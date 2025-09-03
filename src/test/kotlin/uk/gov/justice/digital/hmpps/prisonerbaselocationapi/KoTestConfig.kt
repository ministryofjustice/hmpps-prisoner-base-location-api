package uk.gov.justice.digital.hmpps.prisonerbaselocationapi

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension

class KoTestConfig : AbstractProjectConfig() {
  override val extensions = listOf(SpringExtension())
}
