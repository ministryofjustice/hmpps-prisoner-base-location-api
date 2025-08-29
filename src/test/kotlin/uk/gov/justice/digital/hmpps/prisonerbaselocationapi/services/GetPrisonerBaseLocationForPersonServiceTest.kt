package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.LastMovementType
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.PrisonerBaseLocation
import java.time.LocalDate

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPrisonerBaseLocationForPersonService::class],
)
class GetPrisonerBaseLocationForPersonServiceTest(
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val prisonerBaseLocationProvider: PrisonerBaseLocationProvider,
  private val getPrisonerBaseLocationForPersonService: GetPrisonerBaseLocationForPersonService,
) : DescribeSpec(
  {
    val hmppsId = "A123456"
    val nomisNumber = NomisNumber("A1234BC")

    beforeEach {
      Mockito.reset(getPersonService, prisonerBaseLocationProvider)
    }

    it("returns prisoner base location") {
      val prisonerBaseLocation = PrisonerBaseLocation(
        inPrison = false,
        lastPrisonId = "AAA",
        lastMovementType = LastMovementType.ADMISSION,
        receptionDate = LocalDate.of(2025, 9, 30),
      )

      whenever(getPersonService.getNomisNumber(hmppsId)).thenReturn(Result.success(nomisNumber))
      whenever(prisonerBaseLocationProvider.getBaseLocation(nomisNumber)).thenReturn(Result.success(prisonerBaseLocation))

      val response = getPrisonerBaseLocationForPersonService.execute(hmppsId)

      response.isSuccess shouldBe true
      response.onSuccess { it shouldBe prisonerBaseLocation }
    }

    it("returns errors from get nomis number") {
      val error = Exception("get nomis number - something went wrong")

      whenever(getPersonService.getNomisNumber(hmppsId)).thenReturn(Result.failure(error))

      val response = getPrisonerBaseLocationForPersonService.execute(hmppsId)

      response.isFailure shouldBe true
      response.onFailure { it shouldBe error }

      verify(getPersonService, times(1)).getNomisNumber(hmppsId)
      verify(prisonerBaseLocationProvider, times(0)).getBaseLocation(any())
    }

    it("returns errors from get base location") {
      val error = Exception("get base location - something went wrong")

      whenever(getPersonService.getNomisNumber(hmppsId)).thenReturn(Result.success(nomisNumber))
      whenever(prisonerBaseLocationProvider.getBaseLocation(nomisNumber)).thenReturn(Result.failure(error))

      val response = getPrisonerBaseLocationForPersonService.execute(hmppsId)

      response.isFailure shouldBe true
      response.onFailure { it shouldBe error }

      verify(getPersonService, times(1)).getNomisNumber(hmppsId)
      verify(prisonerBaseLocationProvider, times(1)).getBaseLocation(nomisNumber)
    }
  },
)
