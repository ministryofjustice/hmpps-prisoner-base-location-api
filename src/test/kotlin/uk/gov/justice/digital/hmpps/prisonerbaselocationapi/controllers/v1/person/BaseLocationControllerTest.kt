package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.LastMovementType
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.PrisonerBaseLocation
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services.GetPrisonerBaseLocationForPersonService
import java.time.LocalDate

@WebMvcTest(controllers = [BaseLocationController::class])
@ActiveProfiles("test")
class BaseLocationControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getPrisonerBaseLocationForPersonService: GetPrisonerBaseLocationForPersonService,
) : DescribeSpec(
  {
    val hmppsId = "A1234AA"
    val path = "/v1/persons/$hmppsId/prisoner-base-location"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)
    val filters = null

    fun prisonerBaseLocationReceived() = PrisonerBaseLocation(
      inPrison = true,
      prisonId = "MDI",
      lastPrisonId = "MDI",
      lastMovementType = LastMovementType.ADMISSION,
      receptionDate = LocalDate.of(2025, 9, 30),
    )

    describe("GET $path") {
      beforeTest {
        reset(getPrisonerBaseLocationForPersonService)
        whenever(getPrisonerBaseLocationForPersonService.execute(hmppsId, filters)).thenReturn(Response(data = prisonerBaseLocationReceived()))
      }

      it("returns a 200 OK status code") {
        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("gets the prisoner base location for a person with the matching ID") {
        mockMvc.performAuthorised(path)

        verify(getPrisonerBaseLocationForPersonService, times(1)).execute(hmppsId, filters)
      }

      it("returns the prisoner base location for a person with the matching ID") {
        val result = mockMvc.performAuthorised(path)

        result.response.contentAsString.shouldContain(
          """
          "data": {
               "inPrison": true,
               "prisonId": "MDI",
               "lastPrisonId": "MDI",
               "lastMovementType": "Admission",
               "receptionDate": "2025-09-30"
          }
          """.removeWhitespaceAndNewlines(),
        )
      }

      it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
        whenever(getPrisonerBaseLocationForPersonService.execute(hmppsId, filters)).thenReturn(
          Response(
            data = null,
            errors =
            listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
          ),
        )

        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("returns a 400 BAD Request status code when an invalid hmpps id is found in the upstream API") {
        whenever(getPrisonerBaseLocationForPersonService.execute(hmppsId, filters)).thenReturn(
          Response(
            data = null,
            errors =
            listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.PRISON_API,
                type = UpstreamApiError.Type.BAD_REQUEST,
              ),
            ),
          ),
        )

        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }

      it("fails with the appropriate error when an upstream service is down") {
        whenever(getPrisonerBaseLocationForPersonService.execute(hmppsId, filters)).doThrow(
          WebClientResponseException(500, "MockError", null, null, null, null),
        )

        val response = mockMvc.performAuthorised(path)

        assert(response.response.status == 500)
        assert(
          response.response.contentAsString.equals(
            "{\"status\":500,\"errorCode\":null,\"userMessage\":\"500 MockError\",\"developerMessage\":\"Unable to complete request as an upstream service is not responding\",\"moreInfo\":null}",
          ),
        )
      }
    }
  },
)
