package com.gym.crm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Trainee summary information")
public class TraineeSummaryDto {
    
    @Schema(description = "Trainee's username", example = "john.doe")
    private String username;
    
    @Schema(description = "Trainee's first name", example = "John")
    private String firstName;
    
    @Schema(description = "Trainee's last name", example = "Doe")
    private String lastName;
}
