package com.gym.crm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Change password request")
public class ChangePasswordRequest {
    
    @NotBlank(message = "Username is required")
    @Schema(description = "User's username", example = "john.doe", required = true)
    private String username;
    
    @NotBlank(message = "Old password is required")
    @Schema(description = "Current password", example = "oldPassword123", required = true)
    private String oldPassword;
    
    @NotBlank(message = "New password is required")
    @Schema(description = "New password", example = "newPassword123", required = true)
    private String newPassword;
}
