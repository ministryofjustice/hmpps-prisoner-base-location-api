package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration.services

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration.wiremock.PrisonOffenderSearchApiExtension.Companion.prisonOffenderSearch

class PrisonOffenderSearchServiceTest : IntegrationTestBase() {
  @Test
  fun `should supply authentication token`() {
    hmppsAuth.stubGrantToken()
    prisonOffenderSearch.stubGetPrisonOffender()

    webTestClient.get()
      .uri("v1/persons/A1234AA/prisoner-base-location")
      .headers(setAuthorisation(roles = listOf("ROLE_PRISONER_BASE_LOCATION__LOCATIONS_RO")))
      .exchange()
      .expectStatus().isOk

    prisonOffenderSearch.verify(
      getRequestedFor(urlEqualTo("/prisoner/A1234AA"))
        .withHeader("Authorization", WireMock.equalTo("Bearer ABCDE")),
    )
  }
}
