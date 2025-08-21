package com.gym.crm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Request for adding a new training")
public class AddTrainingRequest {
    
    @NotBlank(message = "Trainee username is required")
    @Schema(description = "Trainee's username", example = "john.doe", required = true)
    private String traineeUsername;
    
    @NotBlank(message = "Trainer username is required")
    @Schema(description = "Trainer's username", example = "jane.smith", required = true)
    private String trainerUsername;
    
    @NotBlank(message = "Training name is required")
    @Schema(description = "Name of the training", example = "Morning Cardio", required = true)
    private String trainingName;
    
    @NotNull(message = "Training date is required")
    @Schema(description = "Date of the training", example = "2024-01-15", required = true)
    private LocalDate trainingDate;
    
    @NotNull(message = "Training duration is required")
    @Positive(message = "Training duration must be positive")
    @Schema(description = "Duration of training in minutes", example = "60", required = true)
    private Integer trainingDuration;
}
