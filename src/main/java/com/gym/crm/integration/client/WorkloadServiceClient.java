package com.gym.crm.integration.client;

import com.gym.crm.integration.dto.WorkloadRequestDto;
import com.gym.crm.model.Training;
import com.gym.crm.util.TransactionContext;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/**
 * Client responsible for notifying TRAINER-WORKLOAD-SERVICE about training add/delete events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkloadServiceClient {

    private static final String ACTION_ADD = "ADD";
    private static final String ACTION_DELETE = "DELETE";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final WebClient.Builder workloadWebClientBuilder;

    /**
     * Notify workload service after a training is successfully created.
     */
    public void notifyTrainingAdded(Training training) {
        if (training == null || training.getTrainer() == null) {
            log.warn("Skipping workload notification for training add due to missing trainer information");
            return;
        }
        WorkloadRequestDto dto = buildDto(training, ACTION_ADD);
        execute(dto);
    }

    /**
     * Notify workload service after a training is successfully removed.
     */
    public void notifyTrainingDeleted(Training training) {
        if (training == null || training.getTrainer() == null) {
            log.warn("Skipping workload notification for training delete due to missing trainer information");
            return;
        }
        WorkloadRequestDto dto = buildDto(training, ACTION_DELETE);
        execute(dto);
    }

    private WorkloadRequestDto buildDto(Training training, String actionType) {
        return WorkloadRequestDto.builder()
                .trainerUsername(training.getTrainer().getUsername())
                .trainerFirstName(training.getTrainer().getFirstName())
                .trainerLastName(training.getTrainer().getLastName())
                .isActive(training.getTrainer().getIsActive())
                .trainingDate(training.getTrainingDate().format(DATE_FORMATTER))
                .trainingDuration(training.getTrainingDuration())
                .actionType(actionType)
                .build();
    }

    @CircuitBreaker(name = "workloadService", fallbackMethod = "notifyFallback")
    protected void execute(WorkloadRequestDto dto) {
        String transactionId = resolveTransactionId();
        String bearerToken = resolveBearerToken();

        log.info("Calling TRAINER-WORKLOAD-SERVICE with transactionId={} payload={}", transactionId, dto);

        workloadWebClientBuilder.build()
                .post()
                .uri("http://TRAINER-WORKLOAD-SERVICE/api/workload")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Transaction-Id", transactionId)
                .headers(headers -> {
                    if (bearerToken != null) {
                        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
                    }
                })
                .bodyValue(dto)
                .retrieve()
                .toBodilessEntity()
                .block(); // fire-and-forget semantics; fallback ensures main flow not interrupted

        log.info("TRAINER-WORKLOAD-SERVICE call succeeded transactionId={}", transactionId);
    }

    /**
     * Fallback: log the error and allow main business flow to continue.
     */
    @SuppressWarnings("unused")
    protected void notifyFallback(WorkloadRequestDto dto, Throwable throwable) {
        String transactionId = MDC.get("transactionId");
        log.error("Fallback triggered for workload notification transactionId={} payload={} reason={}",
                transactionId, dto, Optional.ofNullable(throwable).map(Throwable::getMessage).orElse("unknown error"));
    }

    private String resolveTransactionId() {
        String txId = TransactionContext.getTransactionId();
        if (txId == null) {
            txId = UUID.randomUUID().toString();
            TransactionContext.setTransactionId(txId);
        }
        MDC.put("transactionId", txId);
        return txId;
    }

    private String resolveBearerToken() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            String header = servletRequestAttributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (header != null && header.startsWith("Bearer ")) {
                return header;
            }
        }
        return null;
    }
}

