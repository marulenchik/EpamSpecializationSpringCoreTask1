package com.gym.crm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Trainer update response")
public class TrainerUpdateResponse {
    
    @Schema(description = "Trainer's username", example = "jane.smith")
    private String username;
    
    @Schema(description = "Trainer's first name", example = "Jane")
    private String firstName;
    
    @Schema(description = "Trainer's last name", example = "Smith")
    private String lastName;
    
    @Schema(description = "Trainer's specialization", example = "Fitness")
    private String specialization;
    
    @Schema(description = "Whether the trainer is active", example = "true")
    private Boolean isActive;
    
    @Schema(description = "List of assigned trainees")
    private List<TraineeSummaryDto> trainees;
}
