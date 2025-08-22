package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class HealthCheckIntegrationTest : IntegrationTestBase() {
  @ParameterizedTest
  @ValueSource(strings = ["/health", "/health/ping", "/health/readiness"])
  fun `health check test`(path: String) {
    callApi(path)
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(
        MockMvcResultMatchers.content().json(
          """
        {"status":"UP"}
        """,
        ),
      )
  }
}
