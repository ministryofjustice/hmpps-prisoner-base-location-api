package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.PrisonerBaseLocation
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services.PrisonOffenderSearchService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestController
@RequestMapping(value = ["v1/persons"])
@Tags(value = [Tag(name = "Base Location Service")])
@PreAuthorize("hasRole('ROLE_PRISONER_BASE_LOCATION__LOCATIONS_RO')")
class PrisonerBaseLocationController(
  private val prisonOffenderSearchService: PrisonOffenderSearchService,
) {
  @GetMapping("{prisonNumber:^[A-Z]\\d{4}[A-Z]{2}$}/prisoner-base-location")
  @Operation(
    summary = "Returns prisoner's base location",
    description = "Requires role ROLE_PRISONER_BASE_LOCATION__LOCATIONS_RO",
    security = [SecurityRequirement(name = "view-prisoner-location-role")],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Prisoner's Base Location",
        useReturnTypeSchema = true,
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden to access this endpoint",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Prisoner information not found",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "500",
        description = "An error has occurred",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getPrisonerBaseLocation(
    @Parameter(description = "A prison number, the identifier of a prisoner in Prison", example = "A1234AA") @PathVariable prisonNumber: String,
  ): PrisonerBaseLocation {
    val offender = prisonOffenderSearchService.getPrisonOffender(prisonNumber)
    val location = offender.toBaseLocation()

    return location
  }
}
