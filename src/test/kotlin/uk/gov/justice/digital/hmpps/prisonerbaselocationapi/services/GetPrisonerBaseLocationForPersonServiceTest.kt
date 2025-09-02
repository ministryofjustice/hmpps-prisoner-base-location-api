package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch.POSPrisoner

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPrisonerBaseLocationForPersonService::class],
)
class GetPrisonerBaseLocationForPersonServiceTest(
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  private val getPrisonerBaseLocationForPersonService: GetPrisonerBaseLocationForPersonService,
) : DescribeSpec(
  {

    beforeEach {
      Mockito.reset(prisonerOffenderSearchGateway)
    }

    describe("getPrisonOffender") {
      val nomisNumber = NomisNumber("A1234BC")
      val prisoner = POSPrisoner(firstName = "John", lastName = "Doe", youthOffender = false)

      it("should return a offender") {
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber))
          .thenReturn(prisoner)

        val response = getPrisonerBaseLocationForPersonService.getPrisonOffender(nomisNumber)

        response shouldBe prisoner
      }

      it("should not catch any thrown exceptions") {
        val thrownEx = NullPointerException()

        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber))
          .thenThrow(thrownEx)

        val exception = shouldThrow<NullPointerException> {
          getPrisonerBaseLocationForPersonService.getPrisonOffender(nomisNumber)
        }

        exception shouldBe thrownEx
      }
    }
  },
)
