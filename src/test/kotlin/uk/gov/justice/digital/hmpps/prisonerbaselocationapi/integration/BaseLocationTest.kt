package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BaseLocationTest : IntegrationTestBase() {

  @Nested
  @DisplayName("GET /v1/persons/{hmppsId}/prisoner-base-location")
  inner class TimeEndpoint {
    val hmppsId = "A1234AA"

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/v1/persons/$hmppsId/prisoner-base-location")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("/v1/persons/$hmppsId/prisoner-base-location")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("/v1/persons/$hmppsId/prisoner-base-location")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return OK`() {
      webTestClient.get()
        .uri("/v1/persons/$hmppsId/prisoner-base-location")
        .headers(setAuthorisation(roles = listOf("ROLE_BASE_LOCATION__GOALS__RO")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .jsonPath("$").value<String> {
          assertThat(it).startsWith("${LocalDate.now()}")
        }
    }
  }
}
