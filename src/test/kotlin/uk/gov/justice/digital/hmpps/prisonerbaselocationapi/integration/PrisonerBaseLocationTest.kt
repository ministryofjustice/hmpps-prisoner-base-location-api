package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration.wiremock.NDeliusApiExtension.Companion.nDelius
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration.wiremock.PrisonOffenderSearchApiExtension.Companion.prisonOffenderSearch

class PrisonerBaseLocationTest : IntegrationTestBase() {

  @Nested
  @DisplayName("GET /v1/persons/{hmppsId}/prisoner-base-location")
  inner class GetPrisonerBaseLocation {

    val validCrn = "AA123456"
    val validNomisNumber = "A1234AA"

    @Nested
    @DisplayName("Valid hmppsId (nomis number)")
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

    @Nested
    @DisplayName("Valid hmppsId (crn)")
    inner class GivenAValidCrn {
      @Test
      fun `prisoner info found - return their base location`() {
        hmppsAuth.stubGrantToken()
        nDelius.stubPrisonerWithNomsNumber()
        prisonOffenderSearch.stubGetPrisonOffender()

        webTestClient
          .get()
          .uri("v1/persons/$validCrn/prisoner-base-location")
          .headers(setAuthorisation(roles = listOf("ROLE_VIEW_PRISONER_LOCATION")))
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `prisoner info not found in nDelius - return 404 not found`() {
        hmppsAuth.stubGrantToken()

        webTestClient
          .get()
          .uri("v1/persons/$validCrn/prisoner-base-location")
          .headers(setAuthorisation(roles = listOf("ROLE_VIEW_PRISONER_LOCATION")))
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      fun `prisoner info found in nDelius, but no nomis number - return 404 not found`() {
        hmppsAuth.stubGrantToken()
        nDelius.stubPrisonerWithoutNomsNumber()

        webTestClient
          .get()
          .uri("v1/persons/$validCrn/prisoner-base-location")
          .headers(setAuthorisation(roles = listOf("ROLE_VIEW_PRISONER_LOCATION")))
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      fun `prisoner info found in nDelius, but not offender search - return 404 not found`() {
        hmppsAuth.stubGrantToken()
        nDelius.stubPrisonerWithNomsNumber()

        webTestClient
          .get()
          .uri("v1/persons/$validCrn/prisoner-base-location")
          .headers(setAuthorisation(roles = listOf("ROLE_VIEW_PRISONER_LOCATION")))
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      fun `nDelius api error - return 500 internal server error`() {
        hmppsAuth.stubGrantToken()
        nDelius.stubUpstreamError()

        webTestClient
          .get()
          .uri("v1/persons/$validCrn/prisoner-base-location")
          .headers(setAuthorisation(roles = listOf("ROLE_VIEW_PRISONER_LOCATION")))
          .exchange()
          .expectStatus().is5xxServerError
      }
    }

    @Test
    fun `Invalid hmppsId - return 400 bad request`() {
      val invalidHmppsId = "XXX"

      webTestClient
        .get()
        .uri("v1/persons/$invalidHmppsId/prisoner-base-location")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_PRISONER_LOCATION")))
        .exchange()
        .expectStatus().isBadRequest
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
