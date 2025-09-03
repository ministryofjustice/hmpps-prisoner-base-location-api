package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.controllers.v1

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import jakarta.validation.ValidationException
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
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services.GetPrisonOffenderService

@AutoConfigureMockMvc
class ApiMock(
  @Autowired val mockMvc: MockMvc,
) {
  fun performAuthorised(path: String): MvcResult = mockMvc.perform(
    get(path).with(
      jwt().authorities(SimpleGrantedAuthority("ROLE_VIEW_PRISONER_LOCATION")),
    ),
  ).andReturn()
}

@WebMvcTest(controllers = [PrisonerBaseLocationController::class])
@ActiveProfiles("test")
class BaseLocationControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val getPrisonOffenderService: GetPrisonOffenderService,
) : DescribeSpec(
  {

    val hmppsId = "A1234AA"
    val path = "/v1/persons/$hmppsId/prisoner-base-location"
    val mockMvc = ApiMock(springMockMvc)

    val prisoner = POSPrisoner(firstName = "John", lastName = "Doe", youthOffender = false)

    describe("GET $path") {
      beforeTest {
        reset(getPersonService, getPrisonOffenderService)

        whenever(getPersonService.getNomisNumber(any()))
          .thenReturn(NomisNumber("123456"))
        whenever(getPrisonOffenderService.getPrisonOffender(any()))
          .thenReturn(prisoner)
      }

      it("returns a 200 OK status code") {
        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("gets the prisoner base location for a person with the matching ID") {
        mockMvc.performAuthorised(path)

        verify(getPersonService, times(1)).getNomisNumber(hmppsId)
        verify(getPrisonOffenderService, times(1)).getPrisonOffender(NomisNumber("123456"))
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

      it("returns a 400 BAD Request status code when an invalid hmpps id is found in the upstream API") {
        whenever(getPersonService.getNomisNumber(any())).thenThrow(
          ValidationException("hmppsId is invalid"),
        )

        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }

      it("fails with the appropriate error when an upstream service is down") {
        whenever(getPersonService.getNomisNumber(any())).thenThrow(
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
