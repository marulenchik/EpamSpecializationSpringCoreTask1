package com.gym.crm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request for trainer registration")
public class TrainerRegistrationRequest {
    
    @NotBlank(message = "First name is required")
    @Schema(description = "Trainer's first name", example = "Jane", required = true)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Schema(description = "Trainer's last name", example = "Smith", required = true)
    private String lastName;
    
    @NotNull(message = "Specialization is required")
    @Schema(description = "Trainer's specialization ID", example = "1", required = true)
    private Long specializationId;
}
