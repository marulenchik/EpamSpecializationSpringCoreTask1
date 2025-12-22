package com.gym.crm.cucumber.component;

import com.gym.crm.cucumber.CucumberSpringConfiguration;
import com.gym.crm.mongo.dto.TrainerTrainingEvent;
import com.gym.crm.mongo.model.TrainerTrainingSummary;
import com.gym.crm.mongo.model.TrainerTrainingSummary.MonthSummary;
import com.gym.crm.mongo.model.TrainerTrainingSummary.YearSummary;
import com.gym.crm.mongo.repository.TrainerTrainingSummaryRepository;
import com.gym.crm.mongo.service.TrainerTrainingSummaryService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = CucumberSpringConfiguration.class)
public class TrainerTrainingSummarySteps {

    @Autowired
    private TrainerTrainingSummaryService service;

    @MockBean
    private TrainerTrainingSummaryRepository repository;

    private TrainerTrainingEvent event;
    private TrainerTrainingSummary capturedSummary;

    @Given("a trainer training event with username {string}, first name {string}, last name {string}, status {string}, date {string}, duration {int}")
    public void aTrainerTrainingEvent(String username, String firstName, String lastName, String status, String date, Integer duration) {
        event = TrainerTrainingEvent.builder()
                .trainerUsername(username)
                .trainerFirstName(firstName)
                .trainerLastName(lastName)
                .trainerStatus(Boolean.parseBoolean(status))
                .trainingDate(LocalDate.parse(date))
                .trainingDuration(duration)
                .build();
    }

    @And("no existing trainer summary for username {string}")
    public void noExistingTrainerSummary(String username) {
        when(repository.findByTrainerUsername(username)).thenReturn(Optional.empty());
        Mockito.lenient().when(repository.save(any())).thenAnswer(invocation -> {
            capturedSummary = invocation.getArgument(0);
            return capturedSummary;
        });
    }

    @And("an existing trainer summary for username {string} with year {int} month {int} total duration {int}")
    public void existingTrainerSummary(String username, Integer year, Integer month, Integer total) {
        TrainerTrainingSummary existing = TrainerTrainingSummary.builder()
                .trainerUsername(username)
                .trainerFirstName("Existing")
                .trainerLastName("Trainer")
                .trainerStatus(true)
                .years(new ArrayList<>())
                .build();
        YearSummary y = YearSummary.builder().year(year).months(new ArrayList<>()).build();
        y.getMonths().add(MonthSummary.builder().month(month).trainingsSummaryDuration(total).build());
        existing.getYears().add(y);

        when(repository.findByTrainerUsername(username)).thenReturn(Optional.of(existing));
        Mockito.lenient().when(repository.save(any())).thenAnswer(invocation -> {
            capturedSummary = invocation.getArgument(0);
            return capturedSummary;
        });
    }

    @When("the training event is processed")
    public void theTrainingEventIsProcessed() {
        capturedSummary = service.processTrainingEvent(event);
    }

    @Then("the trainer summary is created with year {int} month {int} total duration {int}")
    public void theTrainerSummaryIsCreated(Integer year, Integer month, Integer total) {
        Assertions.assertThat(capturedSummary).isNotNull();
        Assertions.assertThat(capturedSummary.getYears()).isNotEmpty();
        YearSummary y = capturedSummary.getYears().getFirst();
        Assertions.assertThat(y.getYear()).isEqualTo(year);
        MonthSummary m = y.getMonths().getFirst();
        Assertions.assertThat(m.getMonth()).isEqualTo(month);
        Assertions.assertThat(m.getTrainingsSummaryDuration()).isEqualTo(total);
    }

    @Then("the trainer summary for year {int} month {int} has total duration {int}")
    public void trainerSummaryHasTotal(Integer year, Integer month, Integer total) {
        Assertions.assertThat(capturedSummary).isNotNull();
        YearSummary y = capturedSummary.getYears().stream()
                .filter(val -> val.getYear() == year)
                .findFirst()
                .orElseThrow();
        MonthSummary m = y.getMonths().stream()
                .filter(val -> val.getMonth() == month)
                .findFirst()
                .orElseThrow();
        Assertions.assertThat(m.getTrainingsSummaryDuration()).isEqualTo(total);
    }
}

