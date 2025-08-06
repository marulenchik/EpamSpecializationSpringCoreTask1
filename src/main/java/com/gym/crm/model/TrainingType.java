package com.gym.crm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "training_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Training type name is required")
    @Column(name = "training_type_name", nullable = false, unique = true)
    private String trainingTypeName;
    
    public TrainingType(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }
}