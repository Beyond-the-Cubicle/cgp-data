package com.beyondthecubicle.collector.routeinformation.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.xml.Jaxb2XmlDecoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
public class WebClientConfig {

    private static final int REACTIVE_HTTP_CLIENT_CONNECT_TIMEOUT_MILLIS = 100000;
    private static final int REACTIVE_HTTP_CLIENT_IN_MEMORY_BUFFER_SIZE = 2 * 1024 * 1024;
    private static final String REACTIVE_HTTP_CLIENT_CONNECTION_PROVIDER_NAME = "reactive-http-pool";
    private static final int REACTIVE_HTTP_CLIENT_CONNECTION_PROVIDER_MAX_CONNECTIONS = 100;
    private static final Duration REACTIVE_HTTP_CLIENT_CONNECTION_PROVIDER_PENDING_ACQUIRE_TIMEOUT = Duration.ofMillis(0);
    private static final int REACTIVE_HTTP_CLIENT_CONNECTION_PROVIDER_PENDING_ACQUIRE_MAX_COUNT = -1;
    private static final Duration REACTIVE_HTTP_CLIENT_CONNECTION_PROVIDER_MAX_IDLE_TIME = Duration.ofMillis(1000);

    @Bean
    public WebClient webClient() {
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
        uriBuilderFactory.setEncodingMode(EncodingMode.VALUES_ONLY);
        HttpClient reactiveHttpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, REACTIVE_HTTP_CLIENT_CONNECT_TIMEOUT_MILLIS);
        return WebClient.builder()
                .uriBuilderFactory(uriBuilderFactory)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().jaxb2Decoder(new Jaxb2XmlDecoder()))
                        .build())
                .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs()
                        .maxInMemorySize(REACTIVE_HTTP_CLIENT_IN_MEMORY_BUFFER_SIZE))
                .clientConnector(new ReactorClientHttpConnector(reactiveHttpClient))
                .build();
    }

    @Bean
    public ConnectionProvider connectionProvider() {
        return ConnectionProvider.builder(REACTIVE_HTTP_CLIENT_CONNECTION_PROVIDER_NAME)
                .maxConnections(REACTIVE_HTTP_CLIENT_CONNECTION_PROVIDER_MAX_CONNECTIONS)
                .pendingAcquireTimeout(REACTIVE_HTTP_CLIENT_CONNECTION_PROVIDER_PENDING_ACQUIRE_TIMEOUT)
                .pendingAcquireMaxCount(REACTIVE_HTTP_CLIENT_CONNECTION_PROVIDER_PENDING_ACQUIRE_MAX_COUNT)
                .maxIdleTime(REACTIVE_HTTP_CLIENT_CONNECTION_PROVIDER_MAX_IDLE_TIME)
                .build();
    }

}
