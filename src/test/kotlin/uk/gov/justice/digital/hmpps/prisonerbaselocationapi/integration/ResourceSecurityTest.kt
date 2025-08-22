package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration

import org.springframework.web.servlet.mvc.method.RequestMappingInfo

private fun RequestMappingInfo.getMappings() = methodsCondition.methods
  .map { it.name }
  .ifEmpty { listOf("") } // if no methods defined then match all rather than none
  .flatMap { method ->
    pathPatternsCondition?.patternValues?.map { "$method $it" } ?: emptyList()
  }
