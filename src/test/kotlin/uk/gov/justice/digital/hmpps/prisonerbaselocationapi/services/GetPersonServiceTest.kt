package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import jakarta.validation.ValidationException
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.exceptions.EntityNotFoundException
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.NomisNumber

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPersonService::class],
)
class GetPersonServiceTest(
  @MockitoBean val deliusGateway: NDeliusGateway,
  private val getPersonService: GetPersonService,
) : DescribeSpec({

  beforeEach {
    Mockito.reset(deliusGateway)
  }

  describe("getNomisNumber") {
    val nomsNumber = "G2996UX"
    val crnNumber = "CD123123"

    describe("when given a invalid hmppsId") {
      it("should throw ValidationException") {
        val invalidNomsNumber = "N1234PSX"

        shouldThrow<ValidationException> {
          getPersonService.getNomisNumber(invalidNomsNumber)
        }
      }
    }

    describe("when given a nomis number") {
      it("should return it") {
        val result = getPersonService.getNomisNumber(nomsNumber)

        result shouldBe NomisNumber(nomsNumber)
      }
    }

    describe("when given a crn number") {
      it("should return a nomis number if the delius search returns one") {
        whenever(deliusGateway.getNomisNumber(crnNumber))
          .thenReturn(NomisNumber(nomsNumber))

        val result = getPersonService.getNomisNumber(crnNumber)

        result shouldBe NomisNumber(nomsNumber)
      }

      it("should return a not found error if the delius search does not return a nomis number") {
        whenever(deliusGateway.getNomisNumber(crnNumber))
          .thenReturn(null)

        shouldThrow<EntityNotFoundException> {
          getPersonService.getNomisNumber(crnNumber)
        }
      }

      it("should not catch any thrown exceptions") {
        val error = NullPointerException()
        whenever(deliusGateway.getNomisNumber(crnNumber))
          .thenThrow(error)

        val exception = shouldThrow<Exception> {
          getPersonService.getNomisNumber(crnNumber)
        }

        exception shouldBe error
      }
    }
  }
})
