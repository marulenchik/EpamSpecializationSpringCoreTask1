package com.gym.crm.mongo.service;

import com.gym.crm.mongo.dto.TrainerTrainingEvent;
import com.gym.crm.mongo.model.TrainerTrainingSummary;
import com.gym.crm.mongo.model.TrainerTrainingSummary.MonthSummary;
import com.gym.crm.mongo.model.TrainerTrainingSummary.YearSummary;
import com.gym.crm.mongo.repository.TrainerTrainingSummaryRepository;
import com.gym.crm.util.TransactionContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class TrainerTrainingSummaryService {

    private final TrainerTrainingSummaryRepository repository;

    /**
     * Process an incoming training event and update the aggregated monthly duration
     * for the trainer. This method is idempotent for the same event payload and
     * will create missing year/month buckets as needed.
     */
    public TrainerTrainingSummary processTrainingEvent(@Valid @NotNull TrainerTrainingEvent event) {
        Objects.requireNonNull(event, "event must not be null");

        TransactionState txnState = resolveTransactionId(event.getTransactionId());
        log.info("Processing training event [{}] for trainer {}", txnState.transactionId(), event.getTrainerUsername());

        try {
            TrainerTrainingSummary summary = repository.findByTrainerUsername(event.getTrainerUsername())
                    .orElseGet(() -> buildNewDocument(event));

            // Keep trainer profile data up to date
            summary.setTrainerUsername(event.getTrainerUsername());
            summary.setTrainerFirstName(event.getTrainerFirstName());
            summary.setTrainerLastName(event.getTrainerLastName());
            summary.setTrainerStatus(event.getTrainerStatus());
            if (summary.getYears() == null) {
                summary.setYears(new ArrayList<>());
            }

            int yearValue = event.getTrainingDate().getYear();
            int monthValue = event.getTrainingDate().getMonthValue();

            YearSummary yearSummary = findOrCreateYear(summary, yearValue);
            MonthSummary monthSummary = findOrCreateMonth(yearSummary, monthValue);

            int previous = Optional.ofNullable(monthSummary.getTrainingsSummaryDuration()).orElse(0);
            int updated = previous + event.getTrainingDuration();
            monthSummary.setTrainingsSummaryDuration(updated);

            TrainerTrainingSummary saved = repository.save(summary);
            log.info("Trainer summary updated [{}]: year={}, month={}, previous={}, added={}, total={}",
                    txnState.transactionId(), yearValue, monthValue, previous, event.getTrainingDuration(), updated);
            return saved;
        } finally {
            MDC.remove("transactionId");
            if (txnState.createdHere()) {
                TransactionContext.clear();
            }
        }
    }

    private TrainerTrainingSummary buildNewDocument(TrainerTrainingEvent event) {
        LocalDate date = event.getTrainingDate();
        YearSummary yearSummary = YearSummary.builder()
                .year(date.getYear())
                .months(new ArrayList<>())
                .build();

        MonthSummary monthSummary = MonthSummary.builder()
                .month(date.getMonthValue())
                .trainingsSummaryDuration(0)
                .build();

        yearSummary.getMonths().add(monthSummary);

        TrainerTrainingSummary summary = TrainerTrainingSummary.builder()
                .trainerUsername(event.getTrainerUsername())
                .trainerFirstName(event.getTrainerFirstName())
                .trainerLastName(event.getTrainerLastName())
                .trainerStatus(event.getTrainerStatus())
                .years(new ArrayList<>())
                .build();
        summary.getYears().add(yearSummary);
        return summary;
    }

    private YearSummary findOrCreateYear(TrainerTrainingSummary summary, int yearValue) {
        YearSummary year = summary.getYears().stream()
                .filter(y -> y.getYear() == yearValue)
                .findFirst()
                .orElseGet(() -> {
                    YearSummary newYear = YearSummary.builder()
                            .year(yearValue)
                            .months(new ArrayList<>())
                            .build();
                    summary.getYears().add(newYear);
                    return newYear;
                });
        if (year.getMonths() == null) {
            year.setMonths(new ArrayList<>());
        }
        return year;
    }

    private MonthSummary findOrCreateMonth(YearSummary yearSummary, int monthValue) {
        return yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == monthValue)
                .findFirst()
                .orElseGet(() -> {
                    MonthSummary newMonth = MonthSummary.builder()
                            .month(monthValue)
                            .trainingsSummaryDuration(0)
                            .build();
                    yearSummary.getMonths().add(newMonth);
                    return newMonth;
                });
    }

    private TransactionState resolveTransactionId(String incomingTransactionId) {
        String current = TransactionContext.getTransactionId();
        boolean createdHere = false;

        String transactionId = current;
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = (incomingTransactionId != null && !incomingTransactionId.isBlank())
                    ? incomingTransactionId
                    : TransactionContext.generateTransactionId();
            TransactionContext.setTransactionId(transactionId);
            createdHere = true;
        }

        MDC.put("transactionId", transactionId);
        return new TransactionState(transactionId, createdHere);
    }

    private record TransactionState(String transactionId, boolean createdHere) {
    }
}

