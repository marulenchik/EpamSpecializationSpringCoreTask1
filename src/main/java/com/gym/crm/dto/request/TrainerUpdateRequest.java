package com.gym.crm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request for updating trainer profile")
public class TrainerUpdateRequest {
    
    @NotBlank(message = "Username is required")
    @Schema(description = "Trainer's username", example = "jane.smith", required = true)
    private String username;
    
    @NotBlank(message = "First name is required")
    @Schema(description = "Trainer's first name", example = "Jane", required = true)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Schema(description = "Trainer's last name", example = "Smith", required = true)
    private String lastName;
    
    @NotNull(message = "Active status is required")
    @Schema(description = "Whether the trainer is active", example = "true", required = true)
    private Boolean isActive;
}
