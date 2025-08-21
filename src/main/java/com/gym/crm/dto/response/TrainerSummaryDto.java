package com.gym.crm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Trainer summary information")
public class TrainerSummaryDto {
    
    @Schema(description = "Trainer's username", example = "jane.smith")
    private String username;
    
    @Schema(description = "Trainer's first name", example = "Jane")
    private String firstName;
    
    @Schema(description = "Trainer's last name", example = "Smith")
    private String lastName;
    
    @Schema(description = "Trainer's specialization", example = "Fitness")
    private String specialization;
}
