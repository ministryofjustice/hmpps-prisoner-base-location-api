package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration.wiremock.PrisonOffenderSearchApiExtension.Companion.prisonOffenderSearch

class PrisonerBaseLocationTest : IntegrationTestBase() {

  @Nested
  @DisplayName("GET /v1/persons/{nomisNumber}/prisoner-base-location")
  inner class GetPrisonerBaseLocation {

    val validNomisNumber = "A1234AA"

    @Nested
    @DisplayName("Valid nomis number")
    inner class GivenAValidNomisNumber {
      @Test
      fun `prisoner info found - return their base location`() {
        hmppsAuth.stubGrantToken()
        prisonOffenderSearch.stubGetPrisonOffender()

        webTestClient
          .get()
          .uri("v1/persons/$validNomisNumber/prisoner-base-location")
          .headers(setAuthorisation(roles = listOf("ROLE_VIEW_PRISONER_LOCATION")))
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `prisoner info not found - return 404 not found`() {
        hmppsAuth.stubGrantToken()

        webTestClient
          .get()
          .uri("v1/persons/$validNomisNumber/prisoner-base-location")
          .headers(setAuthorisation(roles = listOf("ROLE_VIEW_PRISONER_LOCATION")))
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      fun `prison offender search api error - return 500 internal server error`() {
        hmppsAuth.stubGrantToken()
        prisonOffenderSearch.stubUpstreamError()

        webTestClient
          .get()
          .uri("v1/persons/$validNomisNumber/prisoner-base-location")
          .headers(setAuthorisation(roles = listOf("ROLE_VIEW_PRISONER_LOCATION")))
          .exchange()
          .expectStatus().is5xxServerError
      }
    }

    @Test
    fun `No auth token - return 401 unauthorized`() {
      hmppsAuth.stubGrantToken()
      prisonOffenderSearch.stubGetPrisonOffender()

      webTestClient
        .get()
        .uri("v1/persons/$validNomisNumber/prisoner-base-location")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Auth token does not contain correct role - return 403 forbidden`() {
      hmppsAuth.stubGrantToken()
      prisonOffenderSearch.stubGetPrisonOffender()

      webTestClient
        .get()
        .uri("v1/persons/$validNomisNumber/prisoner-base-location")
        .headers(setAuthorisation(roles = listOf("SOME_OTHER_ROLE")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `Invalid nomis number - return 404 not found`() {
      val invalidHmppsId = "XXX"

      webTestClient
        .get()
        .uri("v1/persons/$invalidHmppsId/prisoner-base-location")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_PRISONER_LOCATION")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `Hmpps Auth errors - return 502 bad gateway`() {
      hmppsAuth.stubServiceUnavailable()

      webTestClient
        .get()
        .uri("v1/persons/$validNomisNumber/prisoner-base-location")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_PRISONER_LOCATION")))
        .exchange()
        .expectStatus().is5xxServerError
    }
  }
}
