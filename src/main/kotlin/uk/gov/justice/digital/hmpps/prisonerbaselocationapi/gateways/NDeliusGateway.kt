package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.exception.ResponseException
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.PersonOnProbation
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.UpstreamApi
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

  fun getOffender(crnNumber: String): Result<Offender> {
    val result =
      webClient.requestList<Offender>(
        HttpMethod.POST,
        "/search/probation-cases",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
        mapOf("crn" to crnNumber),
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        val offender = result.data.firstOrNull()!!

        Result.success(offender)
      }

      is WebClientWrapperResponse.Error -> {
        Result.failure(ResponseException("broken", 500))
      }
    }
  }

  fun getPerson(crnNumber: String): Result<PersonOnProbation> {
    val offender = getOffender(crnNumber)

    return offender.map { it.toPersonOnProbation() }
  }
}
