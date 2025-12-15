package com.gym.crm.integration.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j configuration for workload service circuit breaker.
 */
@Configuration
public class Resilience4jConfig {

    @Bean
    public CircuitBreakerConfigCustomizer workloadServiceCircuitBreakerCustomizer() {
        return CircuitBreakerConfigCustomizer
                .of("workloadService", builder -> builder
                        .failureRateThreshold(50.0f)
                        .slowCallRateThreshold(50.0f)
                        .waitDurationInOpenState(Duration.ofSeconds(30))
                        .minimumNumberOfCalls(5)
                        .slidingWindowSize(20)
                        .permittedNumberOfCallsInHalfOpenState(3)
                        .slowCallDurationThreshold(Duration.ofSeconds(4))
                        .recordExceptions(Exception.class)
                        .build());
    }
}

