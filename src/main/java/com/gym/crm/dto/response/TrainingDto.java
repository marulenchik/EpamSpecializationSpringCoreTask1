package com.gym.crm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Training information")
public class TrainingDto {
    
    @Schema(description = "Training name", example = "Morning Cardio")
    private String trainingName;
    
    @Schema(description = "Training date", example = "2024-01-15")
    private LocalDate trainingDate;
    
    @Schema(description = "Training type", example = "Fitness")
    private String trainingType;
    
    @Schema(description = "Training duration in minutes", example = "60")
    private Integer trainingDuration;
    
    @Schema(description = "Trainer name", example = "Jane Smith")
    private String trainerName;
    
    @Schema(description = "Trainee name", example = "John Doe")
    private String traineeName;
}
