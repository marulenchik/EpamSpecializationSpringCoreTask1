package com.gym.crm.mongo.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document that stores per-trainer aggregated training duration,
 * organized by year and month.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trainer_training_summary")
@CompoundIndexes({
        @CompoundIndex(name = "first_last_idx", def = "{'trainerFirstName': 1, 'trainerLastName': 1}")
})
public class TrainerTrainingSummary {

    @Id
    private String id;

    @NotBlank
    private String trainerUsername;

    @NotBlank
    private String trainerFirstName;

    @NotBlank
    private String trainerLastName;

    @NotNull
    private Boolean trainerStatus;

    @Builder.Default
    @Valid
    private List<YearSummary> years = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearSummary {
        @Min(1900)
        private int year;

        @Builder.Default
        @Valid
        private List<MonthSummary> months = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthSummary {
        @Min(1)
        @Max(12)
        private int month;

        /**
         * Aggregate duration (e.g., minutes) for the month.
         */
        @NotNull
        private Integer trainingsSummaryDuration;
    }
}

