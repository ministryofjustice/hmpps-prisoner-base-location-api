package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {
  private val version: String = buildProperties.version

  // config inspired by:
  // https://github.com/ministryofjustice/hmpps-prisoner-search/blob/main/hmpps-prisoner-search/src/main/kotlin/uk/gov/justice/digital/hmpps/prisonersearch/search/config/OpenApiConfiguration.kt

  @Bean
  fun customOpenAPI(): OpenAPI = OpenAPI()
    .servers(
      listOf(
        Server().url("https://prisoner-base-location-api-dev.hmpps.service.justice.gov.uk").description("Development"),
        Server().url("https://prisoner-base-location-api-preprod.hmpps.service.justice.gov.uk").description("Pre-Production"),
        Server().url("https://prisoner-base-location-api.hmpps.service.justice.gov.uk").description("Production"),
        Server().url("http://localhost:8080").description("Local"),
      ),
    )
    .tags(
      listOf(),
    )
    .info(
      Info().title("HMPPS Prisoner Base Location Api").version(version)
        .contact(Contact().name("HMPPS Digital Studio").email("feedback@digital.justice.gov.uk")),
    )
    .components(
      Components().addSecuritySchemes(
        "view-prisoner-location-role",
        SecurityScheme().addBearerJwtRequirement("ROLE_VIEW_PRISONER_LOCATION"),
      ),
    )
    .addSecurityItem(SecurityRequirement().addList("view-prisoner-location-role", listOf("read")))
}

private fun SecurityScheme.addBearerJwtRequirement(role: String): SecurityScheme = type(SecurityScheme.Type.HTTP)
  .scheme("bearer")
  .bearerFormat("JWT")
  .`in`(SecurityScheme.In.HEADER)
  .name("Authorization")
  .description("A HMPPS Auth access token with the `$role` role.")
