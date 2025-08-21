package com.gym.crm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request for activating/deactivating user")
public class ActivationRequest {
    
    @NotBlank(message = "Username is required")
    @Schema(description = "User's username", example = "john.doe", required = true)
    private String username;
    
    @NotNull(message = "Active status is required")
    @Schema(description = "Desired active status", example = "true", required = true)
    private Boolean isActive;
}
