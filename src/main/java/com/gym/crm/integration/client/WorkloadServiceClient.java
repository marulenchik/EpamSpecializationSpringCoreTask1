package com.gym.crm.integration.client;

import com.gym.crm.integration.dto.WorkloadRequestDto;
import com.gym.crm.model.Training;
import com.gym.crm.util.TransactionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Client responsible for notifying TRAINER-WORKLOAD-SERVICE via ActiveMQ about training add/delete events.
 * Sending is asynchronous and should never block or interrupt the main business flow.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkloadServiceClient {

    private static final String ACTION_ADD = "ADD";
    private static final String ACTION_DELETE = "DELETE";
    private static final String QUEUE_NAME = "trainer.workload.queue";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final JmsTemplate jmsTemplate;
    @Qualifier("workloadTaskExecutor")
    private final org.springframework.core.task.TaskExecutor taskExecutor;

    /**
     * Notify workload service after a training is successfully created.
     */
    public void notifyTrainingAdded(Training training) {
        if (training == null || training.getTrainer() == null) {
            log.warn("Skipping workload notification for training add due to missing trainer information");
            return;
        }
        WorkloadRequestDto dto = buildDto(training, ACTION_ADD);
        sendAsync(dto);
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
        sendAsync(dto);
    }

    private WorkloadRequestDto buildDto(Training training, String actionType) {
        String transactionId = resolveTransactionId();
        return WorkloadRequestDto.builder()
                .trainerUsername(training.getTrainer().getUsername())
                .trainerFirstName(training.getTrainer().getFirstName())
                .trainerLastName(training.getTrainer().getLastName())
                .isActive(training.getTrainer().getIsActive())
                .trainingDate(training.getTrainingDate().format(DATE_FORMATTER))
                .trainingDuration(training.getTrainingDuration())
                .actionType(actionType)
                .transactionId(transactionId)
                .build();
    }

    private void sendAsync(WorkloadRequestDto dto) {
        taskExecutor.execute(() -> send(dto));
    }

    /**
     * Exposed for integration testing or direct DTO-based publishing.
     */
    public void sendWorkloadUpdate(WorkloadRequestDto dto) {
        sendAsync(dto);
    }

    private void send(WorkloadRequestDto dto) {
        Assert.notNull(dto, "WorkloadRequestDto must not be null");
        String txId = dto.getTransactionId() != null ? dto.getTransactionId() : resolveTransactionId();
        log.info("Sending workload message to queue={} transactionId={} payload={}", QUEUE_NAME, txId, dto);

        try {
            jmsTemplate.convertAndSend(QUEUE_NAME, dto, message -> {
                message.setStringProperty("X-Transaction-Id", txId);
                return message;
            });
            log.info("Workload message sent transactionId={}", txId);
        } catch (Exception ex) {
            log.error("Failed to send workload message transactionId={} reason={}", txId, ex.getMessage());
        }

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
}

