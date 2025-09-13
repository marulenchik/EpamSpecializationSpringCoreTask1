package com.gym.crm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login response with JWT token")
public class LoginResponse {
    
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";
    
    @Schema(description = "Username of authenticated user", example = "john.doe")
    private String username;
    
    @Schema(description = "User type", example = "TRAINEE")
    private String userType;
    
    public LoginResponse(String token, String username, String userType) {
        this.token = token;
        this.username = username;
        this.userType = userType;
        this.tokenType = "Bearer";
    }
}

