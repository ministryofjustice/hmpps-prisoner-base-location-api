package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import uk.gov.justice.hmpps.kotlin.auth.authorisedWebClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import uk.gov.justice.hmpps.kotlin.auth.usernameAwareTokenRequestOAuth2AuthorizedClientManager
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @param:Value("\${api.hmpps-auth.base-url}") private val hmppsAuthBaseUri: String,
  @param:Value("\${api.hmpps-auth.health-timeout:20s}") private val hmppsAuthHealthTimeout: Duration,
  @param:Value("\${api.prisoner-offender-search.base-url}") private val prisonOffenderSearchApiBaseUri: String,
  @param:Value("\${api.prisoner-offender-search.timeout:30s}") private val prisonOffenderSearchApiTimeout: Duration,
) {
  @Bean
  fun hmppsAuthHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(hmppsAuthBaseUri, hmppsAuthHealthTimeout)

  @Bean
  fun prisonOffenderSearchWebClient(authorizedClientManager: OAuth2AuthorizedClientManager, builder: Builder): WebClient = builder.authorisedWebClient(
    authorizedClientManager,
    "hmpps-prisoner-base-location-api",
    prisonOffenderSearchApiBaseUri,
    prisonOffenderSearchApiTimeout,
  )

  @Bean
  @RequestScope
  fun prisonApiWebClient(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
    builder: Builder,
  ) = builder.authorisedWebClient(
    usernameAwareTokenRequestOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService),
    "hmpps-prisoner-base-location-api",
    prisonOffenderSearchApiBaseUri,
    prisonOffenderSearchApiTimeout,
  )
}
