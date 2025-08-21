package com.gym.crm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Request for trainee registration")
public class TraineeRegistrationRequest {
    
    @NotBlank(message = "First name is required")
    @Schema(description = "Trainee's first name", example = "John", required = true)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Schema(description = "Trainee's last name", example = "Doe", required = true)
    private String lastName;
    
    @Schema(description = "Trainee's date of birth", example = "1990-05-15")
    private LocalDate dateOfBirth;
    
    @Schema(description = "Trainee's address", example = "123 Main St, City")
    private String address;
}
