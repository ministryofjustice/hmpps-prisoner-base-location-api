package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration.wiremock.NDeliusApiExtension.Companion.nDelius
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration.wiremock.PrisonOffenderSearchApiExtension.Companion.prisonOffenderSearch

class PrisonerBaseLocationTest: IntegrationTestBase() {

  @Nested
  @DisplayName("GET /v1/persons/{hmppsId}/prisoner-base-location")
  inner class GetPrisonerBaseLocation {

    val validCrn = "AA123456"
    val validNomisNumber = "A1234AA"

    @Nested
    @DisplayName("GIVEN we receive a valid nomis number")
    inner class GivenAValidNomisNumber {
      @Test
      fun `WHEN we find a prisoner THEN we return their base location`() {
        hmppsAuth.stubGrantToken()
        prisonOffenderSearch.stubGetPrisonOffender()

        webTestClient
          .get()
          .uri("v1/persons/${validNomisNumber}/prisoner-base-location")
          .headers(setAuthorisation(roles = listOf("ROLE_VIEW_PRISONER_LOCATION")))
          .exchange()
          .expectStatus().isOk
      }
      @Test
      fun `WHEN we do not find a prisoner THEN we return 404 not found`() {
        hmppsAuth.stubGrantToken()

        webTestClient
          .get()
          .uri("v1/persons/${validNomisNumber}/prisoner-base-location")
          .headers(setAuthorisation(roles = listOf("ROLE_VIEW_PRISONER_LOCATION")))
          .exchange()
          .expectStatus().isNotFound
      }
    }

    @Nested
    @DisplayName("GIVEN we receive a valid crn")
    inner class GivenAValidCrn {
      @Test
      fun `WHEN we find a prisoner THEN we return their base location`() {
        hmppsAuth.stubGrantToken()
        nDelius.stubPrisonerWithNomsNumber()
        prisonOffenderSearch.stubGetPrisonOffender()

        webTestClient
          .get()
          .uri("v1/persons/${validCrn}/prisoner-base-location")
          .headers(setAuthorisation(roles = listOf("ROLE_VIEW_PRISONER_LOCATION")))
          .exchange()
          .expectStatus().isOk
      }
      @Test
      fun `WHEN the prisoner does not have a nomis number THEN we return 404 not found`() {
        hmppsAuth.stubGrantToken()
        nDelius.stubPrisonerWithoutNomsNumber()

        webTestClient
          .get()
          .uri("v1/persons/${validCrn}/prisoner-base-location")
          .headers(setAuthorisation(roles = listOf("ROLE_VIEW_PRISONER_LOCATION")))
          .exchange()
          .expectStatus().isNotFound
      }
      @Test
      fun `WHEN we do not find their details in nDelius THEN we return 404 not found`() {
        hmppsAuth.stubGrantToken()

        webTestClient
          .get()
          .uri("v1/persons/${validCrn}/prisoner-base-location")
          .headers(setAuthorisation(roles = listOf("ROLE_VIEW_PRISONER_LOCATION")))
          .exchange()
          .expectStatus().isNotFound
      }
      @Test
      fun `WHEN we do not find their details in prisoner search THEN we 404 return not found`() {
        hmppsAuth.stubGrantToken()
        nDelius.stubPrisonerWithNomsNumber()

        webTestClient
          .get()
          .uri("v1/persons/${validCrn}/prisoner-base-location")
          .headers(setAuthorisation(roles = listOf("ROLE_VIEW_PRISONER_LOCATION")))
          .exchange()
          .expectStatus().isNotFound
      }
    }

    @Test
    fun `WHEN hmppsId is invalid THEN return a 400 bad request`() {
      val invalidHmppsId = "XXX"

      webTestClient
        .get()
        .uri("v1/persons/${invalidHmppsId}/prisoner-base-location")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_PRISONER_LOCATION")))
        .exchange()
        .expectStatus().isBadRequest
    }


    @Test
    fun `WHEN user does not have correct role THEN return a 403 forbidden`() {
      hmppsAuth.stubGrantToken()
      prisonOffenderSearch.stubGetPrisonOffender()

      webTestClient
        .get()
        .uri("v1/persons/${validNomisNumber}/prisoner-base-location")
        .headers(setAuthorisation(roles = listOf("SOME_OTHER_ROLE")))
        .exchange()
        .expectStatus().isForbidden
    }
  }

}