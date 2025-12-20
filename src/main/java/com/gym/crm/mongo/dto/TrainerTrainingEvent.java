package com.gym.crm.mongo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerTrainingEvent {

    @NotBlank
    private String trainerUsername;

    @NotBlank
    private String trainerFirstName;

    @NotBlank
    private String trainerLastName;

    @NotNull
    private Boolean trainerStatus;

    @NotNull
    private LocalDate trainingDate;

    /**
     * Duration of a single training session (e.g., minutes).
     */
    @NotNull
    @Min(1)
    private Integer trainingDuration;

    /**
     * Propagated transaction identifier for distributed logging.
     */
    private String transactionId;
}

