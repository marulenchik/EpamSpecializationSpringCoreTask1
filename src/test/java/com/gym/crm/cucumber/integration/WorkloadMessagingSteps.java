package com.gym.crm.cucumber.integration;

import com.gym.crm.cucumber.CucumberSpringConfiguration;
import com.gym.crm.integration.client.WorkloadServiceClient;
import com.gym.crm.integration.dto.WorkloadRequestDto;
import com.gym.crm.util.TransactionContext;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.jms.Message;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = CucumberSpringConfiguration.class)
public class WorkloadMessagingSteps {

    @Autowired
    private WorkloadServiceClient workloadServiceClient;

    @Autowired
    private JmsTemplate jmsTemplate;

    private WorkloadRequestDto request;

    @Given("a workload update request for trainer {string} first name {string} last name {string} active {string} date {string} duration {int} action {string}")
    public void workloadUpdateRequest(String username, String firstName, String lastName, String active,
                                      String date, Integer duration, String action) {
        request = WorkloadRequestDto.builder()
                .trainerUsername(username)
                .trainerFirstName(firstName)
                .trainerLastName(lastName)
                .isActive(Boolean.parseBoolean(active))
                .trainingDate(date)
                .trainingDuration(duration)
                .actionType(action)
                .build();
    }

    @And("transaction id {string}")
    public void transactionIdProvided(String txn) {
        TransactionContext.setTransactionId(txn);
        if (request != null) {
            request.setTransactionId(txn);
        }
    }

    @When("the workload message is sent")
    public void sendWorkloadMessage() {
        // Clear any residual messages
        drainQueue();
        workloadServiceClient.sendWorkloadUpdate(request);
    }

    @Then("the queue receives a workload message for trainer {string} with duration {int} and transaction id {string}")
    @SneakyThrows
    public void queueReceivesMessage(String expectedTrainer, Integer expectedDuration, String expectedTxn) {
        jmsTemplate.setReceiveTimeout(3000);
        WorkloadRequestDto received = null;
        Message rawMessage = null;
        long end = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < end && received == null) {
            rawMessage = jmsTemplate.receive("trainer.workload.queue");
            if (rawMessage != null) {
                Object converted = jmsTemplate.getMessageConverter().fromMessage(rawMessage);
                if (converted instanceof WorkloadRequestDto dto) {
                    received = dto;
                    break;
                }
            }
            Thread.sleep(100);
        }

        Assertions.assertThat(received).as("message should be received").isNotNull();
        Assertions.assertThat(received.getTrainerUsername()).isEqualTo(expectedTrainer);
        Assertions.assertThat(received.getTrainingDuration()).isEqualTo(expectedDuration);
        Assertions.assertThat(received.getTransactionId()).isEqualTo(expectedTxn);

        Assertions.assertThat(rawMessage).isNotNull();
        Assertions.assertThat(rawMessage.getStringProperty("X-Transaction-Id")).isEqualTo(expectedTxn);
    }

    @After
    public void cleanup() {
        TransactionContext.clear();
        drainQueue();
    }

    private void drainQueue() {
        jmsTemplate.setReceiveTimeout(100);
        while (true) {
            Message msg = jmsTemplate.receive("trainer.workload.queue");
            if (msg == null) {
                break;
            }
        }
    }
}

