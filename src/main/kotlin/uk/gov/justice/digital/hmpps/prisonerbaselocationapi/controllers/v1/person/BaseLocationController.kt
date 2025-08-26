package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import jakarta.validation.ValidationException
import mu.KotlinLogging
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.controllers.HAS_VIEW_BASE_LOCATION
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.PrisonerBaseLocation
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.services.GetPrisonerBaseLocationForPersonService

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/v1/persons")
@Tags(value = [Tag(name = "Persons"), Tag(name = "Base Location")])
@PreAuthorize(HAS_VIEW_BASE_LOCATION)
class BaseLocationController(
  private val getPrisonerBaseLocationForPersonService: GetPrisonerBaseLocationForPersonService,
) {
  @GetMapping("{hmppsId}/prisoner-base-location")
  @Operation(
    summary = "Returns prisoner's base location of a person",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found prisoner's base location."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
    ],
  )
  fun getPrisonerBaseLocation(
    @Parameter(description = "A HMPPS id", example = "A123123") @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<PrisonerBaseLocation?> {
    log.debug { "request: $hmppsId" }
    val response = getPrisonerBaseLocationForPersonService.execute(hmppsId, filters)
    log.debug { "response: $response" }
    when {
      response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND) -> throw EntityNotFoundException("Could not find prisoner base location for id: $hmppsId")
      response.hasError(UpstreamApiError.Type.BAD_REQUEST) -> throw ValidationException("Invalid HMPPS ID: $hmppsId")
    }

    return DataResponse(data = response.data)
  }
}
