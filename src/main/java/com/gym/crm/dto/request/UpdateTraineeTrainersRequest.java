package com.gym.crm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Request for updating trainee's trainer list")
public class UpdateTraineeTrainersRequest {
    
    @NotBlank(message = "Trainee username is required")
    @Schema(description = "Trainee's username", example = "john.doe", required = true)
    private String traineeUsername;
    
    @NotEmpty(message = "Trainers list is required")
    @Schema(description = "List of trainer usernames", example = "[\"jane.smith\", \"mike.johnson\"]", required = true)
    private List<String> trainerUsernames;
}
