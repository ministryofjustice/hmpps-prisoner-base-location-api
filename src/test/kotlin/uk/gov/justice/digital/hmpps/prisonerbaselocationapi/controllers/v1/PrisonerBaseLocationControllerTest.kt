package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.controllers.v1

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.exceptions.EntityNotFoundException
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services.GetPrisonOffenderService

@AutoConfigureMockMvc
class ApiMock(
  @Autowired val mockMvc: MockMvc,
) {
  fun performAuthorised(path: String): MvcResult = mockMvc.perform(
    get(path).with(
      jwt().authorities(SimpleGrantedAuthority("ROLE_PRISONER_BASE_LOCATION__LOCATIONS_RO")),
    ),
  ).andReturn()
}

@WebMvcTest(controllers = [PrisonerBaseLocationController::class])
@ActiveProfiles("test")
class BaseLocationControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getPrisonOffenderService: GetPrisonOffenderService,
) : DescribeSpec(
  {

    val nomsId = "A1234AA"
    val path = "/v1/persons/$nomsId/prisoner-base-location"
    val mockMvc = ApiMock(springMockMvc)

    val prisoner = POSPrisoner(firstName = "John", lastName = "Doe", youthOffender = false)

    describe("GET $path") {
      beforeTest {
        reset(getPrisonOffenderService)

        whenever(getPrisonOffenderService.getPrisonOffender(any()))
          .thenReturn(prisoner)
      }

      it("returns a 200 OK status code") {
        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("gets the prisoner base location for a person with the matching ID") {
        mockMvc.performAuthorised(path)

        verify(getPrisonOffenderService, times(1)).getPrisonOffender(nomsId)
      }

      it("returns the prisoner base location for a person with the matching ID") {
        val result = mockMvc.performAuthorised(path)

        result.response.contentAsString.shouldBe("""{"inPrison":false}""")
      }

      it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
        whenever(getPrisonOffenderService.getPrisonOffender(any())).thenThrow(
          EntityNotFoundException("not found"),
        )

        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("returns a 404 NOT FOUND status code when an invalid nomsId is used") {
        val result = mockMvc.performAuthorised("/v1/persons/INVALID/prisoner-base-location")

        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("fails with the appropriate error when an upstream service is down") {
        whenever(getPrisonOffenderService.getPrisonOffender(any())).thenThrow(
          WebClientResponseException(500, "MockError", null, null, null, null),
        )

        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.INTERNAL_SERVER_ERROR.value())
        result.response.contentAsString.shouldBe(
          "{\"status\":500,\"errorCode\":null,\"userMessage\":\"Unexpected error: 500 MockError\",\"developerMessage\":\"500 MockError\",\"moreInfo\":null}",
        )
      }
    }
  },
)
