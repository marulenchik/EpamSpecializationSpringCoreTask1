package com.gym.crm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response for user registration")
public class RegistrationResponse {
    
    @Schema(description = "Generated username", example = "john.doe")
    private String username;
    
    @Schema(description = "Generated password", example = "abc123xyz7")
    private String password;
}
