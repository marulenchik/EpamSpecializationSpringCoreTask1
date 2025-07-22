package com.gym.crm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingType {
    private Long id;
    private String trainingTypeName;
    
    public TrainingType(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }
}