package com.gym.crm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response")
public class ErrorResponse {
    
    @Schema(description = "Error message", example = "Invalid request")
    private String message;
    
    @Schema(description = "HTTP status code", example = "400")
    private int status;
    
    @Schema(description = "Timestamp of the error", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;
    
    @Schema(description = "Request path", example = "/api/trainees")
    private String path;
    
    @Schema(description = "Transaction ID for tracking", example = "txn-12345")
    private String transactionId;
}
