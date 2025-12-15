package com.gym.crm.service;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.integration.client.WorkloadServiceClient;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.repository.TrainingRepository;
import com.gym.crm.repository.TrainingTypeRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Validated
@Transactional
public class TrainingService {
    
    private TrainingRepository trainingRepository;
    private TraineeRepository traineeRepository;
    private TrainerRepository trainerRepository;
    private TrainingTypeRepository trainingTypeRepository;
    private AuthenticationService authenticationService;
    private WorkloadServiceClient workloadServiceClient;
    
    @Autowired
    public void setTrainingRepository(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }
    
    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }
    
    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }
    
    @Autowired
    public void setTrainingTypeRepository(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }
    
    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Autowired
    public void setWorkloadServiceClient(WorkloadServiceClient workloadServiceClient) {
        this.workloadServiceClient = workloadServiceClient;
    }
    
    public Training addTraining(@Valid Training training) {
        log.info("Adding training session: {} for trainee {} and trainer {}", 
                training.getTrainingName(), 
                training.getTrainee() != null ? training.getTrainee().getUsername() : "unknown",
                training.getTrainer() != null ? training.getTrainer().getUsername() : "unknown");
        
        Training savedTraining = trainingRepository.save(training);
        log.info("Added training with id: {}", savedTraining.getId());

        // Notify workload service after successful creation (non-blocking for main business flow)
        workloadServiceClient.notifyTrainingAdded(savedTraining);
        return savedTraining;
    }
    
    public List<Training> getTraineeTrainingsList(String traineeUsername, String traineePassword,
                                                LocalDate fromDate, LocalDate toDate,
                                                String trainerName, String trainingTypeName) {
        log.info("Getting trainings list for trainee: {} with criteria", traineeUsername);
        
        // Authenticate trainee
        Optional<Trainee> trainee = authenticationService.getAuthenticatedTrainee(traineeUsername, traineePassword);
        if (trainee.isEmpty()) {
            throw new SecurityException("Authentication failed for trainee: " + traineeUsername);
        }
        
        List<Training> trainings = trainingRepository.findTraineeTrainingsByCriteria(
                traineeUsername, fromDate, toDate, trainerName, trainingTypeName);
        
        log.info("Found {} trainings for trainee: {}", trainings.size(), traineeUsername);
        return trainings;
    }
    
    public List<Training> getTrainerTrainingsList(String trainerUsername, String trainerPassword,
                                                LocalDate fromDate, LocalDate toDate,
                                                String traineeName) {
        log.info("Getting trainings list for trainer: {} with criteria", trainerUsername);
        
        // Authenticate trainer
        Optional<Trainer> trainer = authenticationService.getAuthenticatedTrainer(trainerUsername, trainerPassword);
        if (trainer.isEmpty()) {
            throw new SecurityException("Authentication failed for trainer: " + trainerUsername);
        }
        
        List<Training> trainings = trainingRepository.findTrainerTrainingsByCriteria(
                trainerUsername, fromDate, toDate, traineeName);
        
        log.info("Found {} trainings for trainer: {}", trainings.size(), trainerUsername);
        return trainings;
    }
    
    public Optional<Training> selectTraining(Long id) {
        log.debug("Selecting training with id: {}", id);
        return trainingRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<Training> selectAllTrainings() {
        log.debug("Selecting all trainings");
        return trainingRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<TrainingType> getAllTrainingTypes() {
        log.debug("Getting all training types");
        return trainingTypeRepository.findAll();
    }
} 