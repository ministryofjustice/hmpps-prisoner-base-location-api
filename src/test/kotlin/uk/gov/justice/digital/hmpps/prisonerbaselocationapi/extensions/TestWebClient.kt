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
  responseTimeoutMillis: Int = 15_000,
) {
  private val httpClient: HttpClient
  val client: WebClient

  init {
    httpClient = HttpClient.create()
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)
      .responseTimeout(Duration.ofMillis(responseTimeoutMillis.toLong()))

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
