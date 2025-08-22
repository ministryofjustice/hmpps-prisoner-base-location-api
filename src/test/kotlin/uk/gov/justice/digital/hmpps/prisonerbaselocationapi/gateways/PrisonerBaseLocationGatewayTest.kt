package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.gateways

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.LastMovementType
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services.PrisonerBaseLocationProvider
import java.io.File
import java.time.LocalDate

internal const val FIXTURES_DIR = "src/test/kotlin/uk/gov/justice/digital/hmpps/prisonerbaselocationapi/gateways/fixtures"

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonerBaseLocationProvider::class, PrisonerOffenderSearchGateway::class],
)
class PrisonerBaseLocationGatewayTest(
  @MockitoBean private val hmppsAuthGateway: HmppsAuthGateway,
  private val prisonerBaseLocationProvider: PrisonerBaseLocationProvider,
) : DescribeSpec(
  {
    val knownNomisNumber = "A1234BC"
    val unknownNomisNumnber = "Z9876YX"

    val prisonerOffenderSearchApiMockServer = ApiMockServer.create(UpstreamApi.PRISONER_OFFENDER_SEARCH)

    fun mockPrisoner(
      nomisNumber: String,
      inOutStatus: String = "IN",
      prisonId: String = "MDI",
      lastPrisonId: String = "MDI",
      lastMovementTypeCode: String = "ADM",
      receptionDate: String = "2023-05-01",
    ) = POSPrisoner(
      prisonerNumber = nomisNumber,
      inOutStatus = inOutStatus,
      prisonId = prisonId,
      lastPrisonId = lastPrisonId,
      lastMovementTypeCode = lastMovementTypeCode,
      receptionDate = receptionDate,
      firstName = "First",
      lastName = "Last",
      youthOffender = false,
    )

    fun readFixtures(fileName: String): String = File("${FIXTURES_DIR}/$fileName").readText()

    val knownPrisonerInOutStatus = "IN"
    val knownPrisonerLastMovementTypeCode = "ADM"
    val knownPrisonerLastMovementType = LastMovementType.ADMISSION
    val knownPrisoner = mockPrisoner(nomisNumber = knownNomisNumber, inOutStatus = knownPrisonerInOutStatus, lastMovementTypeCode = knownPrisonerLastMovementTypeCode)
    val knownPrisonerResponse = readFixtures("prisoneroffendersearch/PrisonerByIdResponse.json")
    val unknownPrisonerResponse = readFixtures("prisoneroffendersearch/PrisonerByIdNotFoundResponse.json")

    beforeTest {
      prisonerOffenderSearchApiMockServer.start()
    }

    beforeEach {
      whenever(hmppsAuthGateway.getClientToken("Prisoner Offender Search")).thenReturn(HmppsAuthMockServer.TOKEN)

      with(prisonerOffenderSearchApiMockServer) {
        stubForGet(
          path = "/prisoner/$unknownNomisNumnber",
          status = HttpStatus.NOT_FOUND,
          body = unknownPrisonerResponse,
        )

        stubForGet(
          path = "/prisoner/$knownNomisNumber",
          body = knownPrisonerResponse,
        )
      }
    }

    afterTest {
      prisonerOffenderSearchApiMockServer.stop()
      prisonerOffenderSearchApiMockServer.resetValidator()
    }

    describe("#getPrisonerBaseLocation()") {
      it("does not return prisoner base location for unknown prisoner") {
        val response = prisonerBaseLocationProvider.getPrisonerBaseLocation(unknownNomisNumnber)

        response.data shouldBe null
        response.errors.firstOrNull().shouldNotBeNull().let {
          it.causedBy shouldBe UpstreamApi.PRISONER_OFFENDER_SEARCH
          it.type shouldBe UpstreamApiError.Type.ENTITY_NOT_FOUND
        }
      }

      it("returns prisoner base location for known prisoner") {
        val response = prisonerBaseLocationProvider.getPrisonerBaseLocation(knownNomisNumber)

        response.errors.shouldBeEmpty()
        response.data.shouldNotBeNull().let {
          it.inPrison shouldBe (knownPrisonerInOutStatus == knownPrisoner.inOutStatus)
          it.prisonId shouldBe knownPrisoner.prisonId
          it.lastPrisonId shouldBe knownPrisoner.lastPrisonId
          it.lastMovementType shouldBe knownPrisonerLastMovementType
          it.receptionDate shouldBe knownPrisoner.receptionDate?.let { LocalDate.parse(it) }
        }

        prisonerOffenderSearchApiMockServer.assertValidationPassed()
      }
    }
  },
)
