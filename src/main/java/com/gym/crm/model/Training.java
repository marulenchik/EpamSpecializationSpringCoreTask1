package com.gym.crm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "trainings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Training {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Trainee is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;
    
    @NotNull(message = "Trainer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;
    
    @NotBlank(message = "Training name is required")
    @Column(name = "training_name", nullable = false)
    private String trainingName;
    
    @NotNull(message = "Training type is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingType trainingType;
    
    @NotNull(message = "Training date is required")
    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;
    
    @NotNull(message = "Training duration is required")
    @Positive(message = "Training duration must be positive")
    @Column(name = "training_duration", nullable = false)
    private Integer trainingDuration;
    
    public Training(Trainee trainee, Trainer trainer, String trainingName, TrainingType trainingType, 
                   LocalDate trainingDate, Integer trainingDuration) {
        this.trainee = trainee;
        this.trainer = trainer;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
    }
}