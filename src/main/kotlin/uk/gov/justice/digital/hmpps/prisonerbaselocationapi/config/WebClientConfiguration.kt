package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import uk.gov.justice.hmpps.kotlin.auth.authorisedWebClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import java.time.Duration

val DEFAULT_TIMEOUT = Duration.ofSeconds(30)
val DEFAULT_HEALTH_TIMEOUT = Duration.ofSeconds(2)
val DEFAULT_MIN_BACKOFF_DURATION: Duration = Duration.ofSeconds(3)
const val DEFAULT_MAX_RETRY_ATTEMPTS = 3L

@Configuration
@EnableConfigurationProperties(ApiClientConfig::class)
class WebClientConfiguration(
  @param:Value("\${hmpps-auth.url}") private val hmppsAuthBaseUri: String,
  val apiClientConfig: ApiClientConfig,
) {
  @Bean
  fun hmppsAuthHealthWebClient(builder: Builder): WebClient = builder.healthWebClient(hmppsAuthBaseUri, apiClientConfig.healthTimeout)

  @Bean
  fun prisonOffenderSearchWebClient(
    authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: Builder,
  ): WebClient = "prisoner-offender-search".let { client ->
    builder.authorisedWebClient(
      authorizedClientManager,
      registrationId = "hmpps-prisoner-base-location-api",
      url = apiClientConfig.getBaseUrl(client),
      timeout = apiClientConfig.getResponseTimeout(client),
    )
  }
}

@ConfigurationProperties(prefix = "api.client")
data class ApiClientConfig(
  val healthTimeout: Duration = DEFAULT_HEALTH_TIMEOUT,
  val responseTimeout: Duration = DEFAULT_TIMEOUT,
  val maxRetryAttempts: Long = DEFAULT_MAX_RETRY_ATTEMPTS,
  val minBackOffDuration: Duration = DEFAULT_MIN_BACKOFF_DURATION,
  val statusCodeRetryExhausted: Int = HttpStatus.SERVICE_UNAVAILABLE.value(),
  val clients: Map<String, ApiEndpointConfig> = emptyMap(),
) {
  fun getResponseTimeout(client: String) = clients[client]?.responseTimeout ?: responseTimeout
  fun getBaseUrl(client: String) = clients[client]!!.baseUrl
}

data class ApiEndpointConfig(
  val baseUrl: String,
  val responseTimeout: Duration? = null,
)
