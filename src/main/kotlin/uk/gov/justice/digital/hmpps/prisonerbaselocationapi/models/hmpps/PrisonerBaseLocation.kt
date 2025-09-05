package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class PrisonerBaseLocation(
  @param:Schema(description = "In prison or not")
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  val inPrison: Boolean? = null,
  @param:Schema(description = "Prison ID", example = "MDI")
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  val prisonId: String? = null,
  @param:Schema(description = "The last prison for the prisoner (which is the same as the prisonId if they are still inside prison)", example = "MDI")
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  val lastPrisonId: String? = null,
  @param:Schema(description = "Last movement type", example = "Admission")
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  val lastMovementType: LastMovementType? = null,
  @param:Schema(description = "Reception date", example = "2025-04-01")
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  val receptionDate: LocalDate? = null,
)

@Schema
enum class LastMovementType(
  val value: String,
) {
  @JsonProperty("Admission")
  ADMISSION("Admission"),

  @JsonProperty("Release")
  RELEASE("Release"),

  @JsonProperty("Transfers")
  TRANSFERS("Transfers"),

  @JsonProperty("Court")
  COURT("Court"),

  @JsonProperty("Temporary Absence")
  TEMPORARY_ABSENCE("Temporary Absence"),
}
