package com.gym.crm.mongo.service;

import com.gym.crm.mongo.dto.TrainerTrainingEvent;
import com.gym.crm.mongo.model.TrainerTrainingSummary;
import com.gym.crm.mongo.model.TrainerTrainingSummary.MonthSummary;
import com.gym.crm.mongo.model.TrainerTrainingSummary.YearSummary;
import com.gym.crm.mongo.repository.TrainerTrainingSummaryRepository;
import com.gym.crm.util.TransactionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerTrainingSummaryServiceTest {

    @Mock
    private TrainerTrainingSummaryRepository repository;

    @InjectMocks
    private TrainerTrainingSummaryService service;

    @AfterEach
    void tearDown() {
        TransactionContext.clear();
    }

    @Test
    void createsNewDocumentWhenTrainerNotFound() {
        TrainerTrainingEvent event = TrainerTrainingEvent.builder()
                .trainerUsername("trainer1")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .trainerStatus(true)
                .trainingDate(LocalDate.of(2025, 5, 10))
                .trainingDuration(60)
                .build();

        when(repository.findByTrainerUsername("trainer1")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TrainerTrainingSummary saved = service.processTrainingEvent(event);

        assertThat(saved.getTrainerUsername()).isEqualTo("trainer1");
        assertThat(saved.getYears()).hasSize(1);
        YearSummary year = saved.getYears().getFirst();
        assertThat(year.getYear()).isEqualTo(2025);
        assertThat(year.getMonths()).hasSize(1);
        MonthSummary month = year.getMonths().getFirst();
        assertThat(month.getMonth()).isEqualTo(5);
        assertThat(month.getTrainingsSummaryDuration()).isEqualTo(60);

        verify(repository).save(any(TrainerTrainingSummary.class));
    }

    @Test
    void incrementsExistingMonthDuration() {
        TrainerTrainingSummary existing = TrainerTrainingSummary.builder()
                .trainerUsername("trainer2")
                .trainerFirstName("Jane")
                .trainerLastName("Smith")
                .trainerStatus(true)
                .years(new ArrayList<>())
                .build();
        YearSummary year = YearSummary.builder()
                .year(2025)
                .months(new ArrayList<>())
                .build();
        year.getMonths().add(MonthSummary.builder().month(5).trainingsSummaryDuration(30).build());
        existing.getYears().add(year);

        TrainerTrainingEvent event = TrainerTrainingEvent.builder()
                .trainerUsername("trainer2")
                .trainerFirstName("Jane")
                .trainerLastName("Smith")
                .trainerStatus(true)
                .trainingDate(LocalDate.of(2025, 5, 15))
                .trainingDuration(20)
                .transactionId("txn-test")
                .build();

        when(repository.findByTrainerUsername("trainer2")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TrainerTrainingSummary saved = service.processTrainingEvent(event);
        MonthSummary month = saved.getYears().getFirst().getMonths().getFirst();
        assertThat(month.getTrainingsSummaryDuration()).isEqualTo(50);
        verify(repository).save(any(TrainerTrainingSummary.class));
    }

    @Test
    void addsNewMonthForExistingYear() {
        TrainerTrainingSummary existing = TrainerTrainingSummary.builder()
                .trainerUsername("trainer3")
                .trainerFirstName("Jake")
                .trainerLastName("Miles")
                .trainerStatus(true)
                .years(new ArrayList<>())
                .build();
        YearSummary year = YearSummary.builder()
                .year(2025)
                .months(new ArrayList<>())
                .build();
        year.getMonths().add(MonthSummary.builder().month(4).trainingsSummaryDuration(10).build());
        existing.getYears().add(year);

        TrainerTrainingEvent event = TrainerTrainingEvent.builder()
                .trainerUsername("trainer3")
                .trainerFirstName("Jake")
                .trainerLastName("Miles")
                .trainerStatus(true)
                .trainingDate(LocalDate.of(2025, 5, 2))
                .trainingDuration(25)
                .build();

        when(repository.findByTrainerUsername("trainer3")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TrainerTrainingSummary saved = service.processTrainingEvent(event);

        List<MonthSummary> months = saved.getYears().getFirst().getMonths();
        assertThat(months).hasSize(2);
        MonthSummary may = months.stream().filter(m -> m.getMonth() == 5).findFirst().orElseThrow();
        assertThat(may.getTrainingsSummaryDuration()).isEqualTo(25);
        verify(repository).save(any(TrainerTrainingSummary.class));
    }
}

