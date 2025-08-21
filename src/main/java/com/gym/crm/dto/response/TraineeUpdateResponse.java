package com.gym.crm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Trainee update response")
public class TraineeUpdateResponse {
    
    @Schema(description = "Trainee's username", example = "john.doe")
    private String username;
    
    @Schema(description = "Trainee's first name", example = "John")
    private String firstName;
    
    @Schema(description = "Trainee's last name", example = "Doe")
    private String lastName;
    
    @Schema(description = "Trainee's date of birth", example = "1990-05-15")
    private LocalDate dateOfBirth;
    
    @Schema(description = "Trainee's address", example = "123 Main St, City")
    private String address;
    
    @Schema(description = "Whether the trainee is active", example = "true")
    private Boolean isActive;
    
    @Schema(description = "List of assigned trainers")
    private List<TrainerSummaryDto> trainers;
}
