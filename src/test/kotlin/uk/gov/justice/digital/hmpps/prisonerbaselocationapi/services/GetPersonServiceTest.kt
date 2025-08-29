package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import jakarta.validation.ValidationException
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.PersonOnProbation

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
    Mockito.reset(prisonerOffenderSearchGateway, deliusGateway)
  }

  describe("getNomisNumber") {
    val nomsNumber = "G2996UX"
    val crnNumber = "CD123123"

    describe("when given a invalid hmppsId") {
      it("should return failure") {
        val invalidNomsNumber = "N1234PSX"

        val result = getPersonService.getNomisNumber(invalidNomsNumber)

        result.isFailure shouldBe true
        result.onFailure { error ->
          error shouldBe ValidationException("hmppsId is invalid")
        }
      }
    }

    describe("when given a nomis number") {
      it("should return it") {
        val result = getPersonService.getNomisNumber(nomsNumber)

        result.isSuccess shouldBe true
        result.onSuccess { nomisNumber ->
          nomisNumber shouldBe NomisNumber(nomsNumber)
        }
      }
    }

    describe("when given a crn number") {
      it("should return a nomis number if the delius search returns one") {
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
        whenever(deliusGateway.getPerson(crnNumber)).thenReturn(Result.success(personOnProbation))

        val result = getPersonService.getNomisNumber(crnNumber)

        result.isSuccess shouldBe true
        result.onSuccess { nomisNumber ->
          nomisNumber.shouldBe(NomisNumber(nomsNumber))
        }
      }

      it("should return a not found error if the delius search does not return a nomis number") {
        val personOnProbation = PersonOnProbation(
          Person(
            firstName = "John",
            lastName = "Doe",
          ),
          underActiveSupervision = true,
        )
        whenever(deliusGateway.getPerson(crnNumber)).thenReturn(Result.success(personOnProbation))

        val result = getPersonService.getNomisNumber(crnNumber)

        result.isFailure shouldBe true
        result.onFailure { error ->
          error shouldBe EntityNotFoundException("NOMIS number not found")
        }
      }

      it("return an error if the delius search errors") {
        val error = Exception("something went wrong")
        whenever(deliusGateway.getPerson(crnNumber)).thenReturn(Result.failure(error))

        val result = getPersonService.getNomisNumber(crnNumber)

        result.isFailure shouldBe true
        result.onFailure { e ->
          e shouldBe error
        }
      }
    }
  }
})
