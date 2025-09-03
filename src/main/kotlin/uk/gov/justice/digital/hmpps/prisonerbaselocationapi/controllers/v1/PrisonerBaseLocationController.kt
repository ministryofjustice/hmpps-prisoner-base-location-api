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
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services.GetPrisonOffenderService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestController
@RequestMapping(value = ["v1/persons"])
@Tags(value = [Tag(name = "Persons"), Tag(name = "Base Location Service")])
@PreAuthorize("hasRole('ROLE_VIEW_PRISONER_LOCATION')")
class PrisonerBaseLocationController(
  private val getPersonService: GetPersonService,
  private val getPrisonOffenderService: GetPrisonOffenderService,
) {
  @GetMapping("{hmppsId}/prisoner-base-location")
  @Operation(
    summary = "Returns prisoner's base location",
    description = "Requires role ROLE_VIEW_PRISONER_LOCATION",
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
    @Parameter(description = "A HMPPS id", example = "A123123") @PathVariable hmppsId: String,
  ): PrisonerBaseLocation {
    val nomisNumber = getPersonService.getNomisNumber(hmppsId)
    val offender = getPrisonOffenderService.getPrisonOffender(nomisNumber)
    val location = offender.toBaseLocation()

    return location
  }
}
