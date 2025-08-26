package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.PersonOnProbation
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch.Offender

@Component
class NDeliusGateway(
  @Value("\${services.ndelius.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("nDelius")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }

  fun getOffender(id: String? = null): Response<Offender?> {
    val queryField =
      if (isNomsNumber(id)) {
        "nomsNumber"
      } else {
        "crn"
      }

    val result =
      webClient.requestList<Offender>(
        HttpMethod.POST,
        "/search/probation-cases",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
        mapOf(queryField to id),
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        val persons = result.data
        val person = persons.firstOrNull()?.toPerson()

        Response(
          data = persons.firstOrNull(),
          errors =
          if (person == null) {
            listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.NDELIUS,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            )
          } else {
            emptyList()
          },
        )
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun getPerson(id: String? = null): Response<PersonOnProbation?> {
    val offender = getOffender(id)
    return Response(data = offender.data?.toPersonOnProbation(), errors = offender.errors)
  }

  private fun isNomsNumber(id: String?): Boolean = id?.matches(Regex("^[A-Z]\\d{4}[A-Z]{2}+$")) == true
}
