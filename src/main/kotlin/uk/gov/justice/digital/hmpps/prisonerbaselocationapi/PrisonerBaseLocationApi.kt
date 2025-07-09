package uk.gov.justice.digital.hmpps.prisonerbaselocationapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PrisonerBaseLocationApi

fun main(args: Array<String>) {
  runApplication<PrisonerBaseLocationApi>(*args)
}
