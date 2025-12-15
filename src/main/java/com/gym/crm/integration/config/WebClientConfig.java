package com.gym.crm.integration.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * WebClient configuration with load-balancing support and common filters.
 */
@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder workloadWebClientBuilder() {
        HttpClient httpClient = HttpClient.create();
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                // Additional filters (auth/transactionId) are applied in the client to keep context access simple
                .filter(ExchangeFilterFunction.ofRequestProcessor(Mono::just));
    }
}

