package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.PersonOnProbation
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch.POSPrisoner

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPersonService::class],
)
class GetPersonServiceTest(
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockitoBean val deliusGateway: NDeliusGateway,
  private val getPersonService: GetPersonService,
) : DescribeSpec({

  beforeEach {
    Mockito.reset(prisonerOffenderSearchGateway)
    Mockito.reset(deliusGateway)
  }

  describe("getNomisNumber") {
    val nomsNumber = "G2996UX"
    val crnNumber = "CD123123"

    describe("when given a invalid hmppsId") {
      it("should return bad request") {
        val invalidNomsNumber = "N1234PSX"

        val result = getPersonService.getNomisNumber(invalidNomsNumber)

        result.data.shouldBeNull()
        result.errors.shouldBe(listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.BAD_REQUEST, description = "Invalid HMPPS ID: $invalidNomsNumber")))
      }
    }

    describe("when given a nomis number") {
      it("should return it if a prison offender search is successful") {
        val prisoner = POSPrisoner(
          firstName = "John",
          lastName = "Doe",
          prisonId = "ABC",
          youthOffender = false,
        )
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(
          Response(
            data = prisoner,
            errors = emptyList(),
          ),
        )

        val result = getPersonService.getNomisNumber(nomsNumber)

        result.data.shouldBe(NomisNumber(nomsNumber))
        result.errors.shouldBeEmpty()
      }

      it("return an error if the prison offender search errors") {
        val errors = listOf(
          UpstreamApiError(
            causedBy = UpstreamApi.PRISON_API,
            type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
          ),
        )
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getPersonService.getNomisNumber(nomsNumber)

        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
      }
    }

    describe("when given a crn number") {
      it("should return a nomis number one if the delius search returns one") {
        val personOnProbation = PersonOnProbation(
          Person(
            firstName = "John",
            lastName = "Doe",
            identifiers = Identifiers(
              nomisNumber = nomsNumber,
              deliusCrn = crnNumber,
            ),
          ),
          underActiveSupervision = true,
        )
        whenever(deliusGateway.getPerson(crnNumber)).thenReturn(
          Response(
            data = personOnProbation,
            errors = emptyList(),
          ),
        )

        val result = getPersonService.getNomisNumber(crnNumber)

        result.data.shouldBe(NomisNumber(nomsNumber))
        result.errors.shouldBeEmpty()
      }

      it("should return a 404 error if the delius search does not return a nomis number") {
        val personOnProbation = PersonOnProbation(
          Person(
            firstName = "John",
            lastName = "Doe",
          ),
          underActiveSupervision = true,
        )
        whenever(deliusGateway.getPerson(crnNumber)).thenReturn(
          Response(
            data = personOnProbation,
            errors = emptyList(),
          ),
        )

        val result = getPersonService.getNomisNumber(crnNumber)

        result.data.shouldBeNull()
        result.errors.shouldBe(
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NDELIUS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              "NOMIS number not found",
            ),
          ),
        )
      }

      it("return an error if the delius search errors") {
        val errors =
          listOf(UpstreamApiError(causedBy = UpstreamApi.NDELIUS, type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR))
        whenever(deliusGateway.getPerson(crnNumber)).thenReturn(Response(data = null, errors = errors))

        val result = getPersonService.getNomisNumber(crnNumber)

        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
      }
    }
  }
})
