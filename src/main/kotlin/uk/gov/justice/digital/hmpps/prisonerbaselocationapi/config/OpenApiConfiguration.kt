package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.GlobalOpenApiCustomizer
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.controllers.ErrorResponse

@Configuration
class OpenApiConfiguration(private val buildProperties: BuildProperties) {
  @Bean
  fun openApiCustomizer(): OpenApiCustomizer = object : GlobalOpenApiCustomizer {
    override fun customise(openApi: OpenAPI) {
      openApi
        .info(
          Info()
            .title("HMPPS Prisoner Base Location Api")
            .description("Retrieves a prisoner's Base Location.")
            .license(
              License()
                .name("MIT")
                .url("https://github.com/ministryofjustice/hmpps-prisoner-base-location-api/blob/main/LICENSE"),
            )
            .version(buildProperties.version),
        )
        .servers(
          listOf(
            Server().url("https://prisoner-base-location-api-dev.hmpps.service.justice.gov.uk")
              .description("Development server"),
            Server().url("https://prisoner-base-location-api-preprod.hmpps.service.justice.gov.uk")
              .description("Pre-production server, containing live data"),
            Server().url("https://prisoner-base-location-api.hmpps.service.justice.gov.uk")
              .description("Production"),
            Server().url("http://localhost:8080").description("Local"),
          ),
        )
        .components(
          Components().addSecuritySchemes(
            "dn",
            SecurityScheme()
              .name("subject-distinguished-name")
              .type(SecurityScheme.Type.APIKEY)
              .`in`(SecurityScheme.In.HEADER)
              .description("Example: O=test,CN=automated-test-client"),
          ),
        )
        .security(listOf(SecurityRequirement().addList("dn")))
        .components
        .addSchemas(
          "DataResponsePrisonerBaseLocation",
          Schema<Any>()
            .type("object")
            .properties(
              mapOf("data" to Schema<Any>().`$ref`("#/components/schemas/PrisonerBaseLocation")),
            ),
        )
        .addSchemas(
          "BadRequest",
          Schema<ErrorResponse>().properties(
            mapOf(
              "status" to Schema<Int>().type("number").example(400),
              "userMessage" to Schema<String>().type("string")
                .example("Validation failure: No query parameters specified."),
              "developerMessage" to Schema<String>().type("string").example("No query parameters specified."),
            ),
          ),
        ).addSchemas(
          "PersonNotFound",
          Schema<ErrorResponse>().description("Failed to find a person with the provided HMPPS ID.").properties(
            mapOf(
              "status" to Schema<Int>().type("number").example(404),
              "userMessage" to Schema<String>().type("string")
                .example("404 Not found error: Could not find person with HMPPS id: 2003/0011991D."),
              "developerMessage" to Schema<String>().type("string")
                .example("Could not find person with HMPPS id: 2003/0011991D."),
            ),
          ),
        )
    }
  }
}
