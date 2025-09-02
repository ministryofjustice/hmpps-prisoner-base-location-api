package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class HealthCheckIntegrationTest : IntegrationTestBase() {
  @ParameterizedTest
  @ValueSource(strings = ["/health", "/health/ping", "/health/readiness"])
  fun `health check test`(path: String) {
    webTestClient.get()
      .uri(path)
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .json("""{"status":"UP"}""")
  }
}
