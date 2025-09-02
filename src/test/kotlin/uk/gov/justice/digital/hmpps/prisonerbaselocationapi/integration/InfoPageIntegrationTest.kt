package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration

import kotlin.test.Test

class InfoPageIntegrationTest : IntegrationTestBase() {
  @Test
  fun `Info page is accessible`() {
    webTestClient.get()
      .uri("/info")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$.build.name").isEqualTo("hmpps-prisoner-base-location-api")
  }
}
