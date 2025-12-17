package com.gym.crm.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO sent to TRAINER-WORKLOAD-SERVICE to keep trainer workload in sync.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadRequestDto {
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private Boolean isActive;
    private String trainingDate;      // yyyy-MM-dd
    private Integer trainingDuration; // minutes/hours as number
    private String actionType;        // ADD | DELETE
    private String transactionId;
}

