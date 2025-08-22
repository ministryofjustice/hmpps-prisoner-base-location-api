package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.integration

import io.swagger.v3.parser.OpenAPIV3Parser
import net.minidev.json.JSONArray
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.endsWith
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class OpenApiDocsTest : IntegrationTestBase() {
  @LocalServerPort
  private val port: Int = 0

  @Test
  fun `open api docs are available`() {
    callApi(path = "/swagger-ui/index.html?configUrl=/v3/api-docs")
      .andExpect(status().isOk)
  }

  @Test
  fun `open api docs redirect to correct page`() {
    callApi(path = "/swagger-ui.html")
      .andExpect(status().is3xxRedirection)
      .andExpect(header().string("Location", endsWith("/swagger-ui/index.html")))
  }

  @Test
  @Disabled("TODO Enable this test once you have an endpoint. It checks that endpoints appear on the OpenAPI spec.")
  fun `the open api json contains documentation`() {
    callApi(path = "/v3/api-docs")
      .andExpect(status().isOk)
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.paths").isNotEmpty)
  }

  @Test
  fun `the open api json contains the version number`() {
    callApi(path = "/v3/api-docs")
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.info.version").value(DateTimeFormatter.ISO_DATE.format(LocalDate.now())))
  }

  @Test
  fun `the open api json is valid`() {
    val result = OpenAPIV3Parser().readLocation("http://localhost:$port/v3/api-docs", null, null)
    assertThat(result.messages).isEmpty()
  }

  @Test
  @Disabled("TODO Enable this test once you have added security schema to OpenApiConfiguration.OpenAPi().components()")
  fun `the open api json path security requirements are valid`() {
    val result = OpenAPIV3Parser().readLocation("http://localhost:$port/v3/api-docs", null, null)

    // The security requirements of each path don't appear to be validated like they are at https://editor.swagger.io/
    // We therefore need to grab all the valid security requirements and check that each path only contains those items
    val securityRequirements = result.openAPI.security.flatMap { it.keys }
    result.openAPI.paths.forEach { pathItem ->
      assertThat(pathItem.value.get.security.flatMap { it.keys }).isSubsetOf(securityRequirements)
    }
  }

  @ParameterizedTest
  @Disabled("TODO Enable this test once you have added security schema to OpenApiConfiguration.OpenAPi().components(). Add the security scheme / roles to @CsvSource")
  @CsvSource(value = ["security-scheme-name, ROLE_"])
  fun `the security scheme is setup for bearer tokens`(key: String, role: String) {
    callApi(path = "/v3/api-docs")
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.components.securitySchemes.$key.type").value("http"))
      .andExpect(jsonPath("$.components.securitySchemes.$key.scheme").value("bearer"))
      .andExpect(jsonPath("$.components.securitySchemes.$key.bearerFormat").value("JWT"))
      .andExpect(jsonPath("$.security[0].$key").value(JSONArray().apply { this.add("read") }))
  }

  @Test
  @Disabled("TODO Enable this test once you have an endpoint.")
  fun `all endpoints have a security scheme defined`() {
    callApi(path = "/v3/api-docs")
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.paths[*][*][?(!@.security)]").doesNotExist())
  }
}
