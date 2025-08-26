package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class InfoPageIntegrationTest : IntegrationTestBase() {
  @Test
  fun `Info page is accessible`() {
    callApi("/info")
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.build.name").value("hmpps-prisoner-base-location-api"))
  }
}
