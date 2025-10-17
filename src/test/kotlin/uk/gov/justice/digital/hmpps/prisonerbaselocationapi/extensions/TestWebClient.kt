package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.extensions

import io.netty.channel.ChannelOption
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

class TestWebClient(
  val baseUrl: String,
  connectTimeoutMillis: Int = 10_000,
  responseTimeoutSeconds: Long = 15,
) {
  private val httpClient: HttpClient
  val client: WebClient

  init {
    httpClient = HttpClient.create()
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)
      .responseTimeout(Duration.ofSeconds(responseTimeoutSeconds))

    client = WebClient.builder()
      .baseUrl(baseUrl)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .exchangeStrategies(
        ExchangeStrategies.builder()
          .codecs { configurer ->
            configurer.defaultCodecs().maxInMemorySize(-1)
          }.build(),
      ).build()
  }
}
