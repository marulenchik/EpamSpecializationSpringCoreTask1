package com.gym.crm.service;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.repository.TrainingRepository;
import com.gym.crm.util.UserCredentialGenerator;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@Validated
@Transactional
public class TraineeService {
    
    private TraineeRepository traineeRepository;
    private TrainerRepository trainerRepository;
    private TrainingRepository trainingRepository;
    private UserCredentialGenerator credentialGenerator;
    private AuthenticationService authenticationService;
    
    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }
    
    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }
    
    @Autowired
    public void setTrainingRepository(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }
    
    @Autowired
    public void setCredentialGenerator(UserCredentialGenerator credentialGenerator) {
        this.credentialGenerator = credentialGenerator;
    }
    
    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    
    public Trainee createTrainee(@Valid Trainee trainee) {
        log.info("Creating trainee profile for {} {}", trainee.getFirstName(), trainee.getLastName());
        
        String username = credentialGenerator.generateUsername(
                trainee.getFirstName(), 
                trainee.getLastName(),
                traineeRepository::existsByFirstNameAndLastName
        );
        String password = credentialGenerator.generatePassword();
        
        trainee.setUsername(username);
        trainee.setPassword(password);
        trainee.setIsActive(true);
        
        Trainee savedTrainee = traineeRepository.save(trainee);
        log.info("Created trainee with id: {} and username: {}", savedTrainee.getId(), username);
        return savedTrainee;
    }
    
    public Optional<Trainee> selectTraineeByUsername(String username) {
        log.debug("Selecting trainee with username: {}", username);
        return traineeRepository.findByUsername(username);
    }
    
    public boolean matchTraineeCredentials(String username, String password) {
        log.debug("Matching trainee credentials for username: {}", username);
        return authenticationService.authenticateTrainee(username, password);
    }
    
    public boolean changeTraineePassword(String username, String oldPassword, String newPassword) {
        log.info("Changing password for trainee: {}", username);
        
        Optional<Trainee> trainee = authenticationService.getAuthenticatedTrainee(username, oldPassword);
        if (trainee.isPresent()) {
            trainee.get().setPassword(newPassword);
            traineeRepository.save(trainee.get());
            log.info("Password changed successfully for trainee: {}", username);
            return true;
        }
        
        log.warn("Password change failed for trainee: {} - authentication failed", username);
        return false;
    }
    
    public Trainee updateTrainee(String username, String password, @Valid Trainee updatedTrainee) {
        log.info("Updating trainee profile for username: {}", username);
        
        Optional<Trainee> trainee = authenticationService.getAuthenticatedTrainee(username, password);
        if (trainee.isEmpty()) {
            throw new SecurityException("Authentication failed for trainee: " + username);
        }
        
        Trainee existingTrainee = trainee.get();
        existingTrainee.setFirstName(updatedTrainee.getFirstName());
        existingTrainee.setLastName(updatedTrainee.getLastName());
        existingTrainee.setDateOfBirth(updatedTrainee.getDateOfBirth());
        existingTrainee.setAddress(updatedTrainee.getAddress());
        
        Trainee savedTrainee = traineeRepository.save(existingTrainee);
        log.info("Updated trainee profile for username: {}", username);
        return savedTrainee;
    }
    
    public boolean activateTrainee(String username, String password) {
        log.info("Activating trainee: {}", username);
        return toggleTraineeStatus(username, password, true);
    }
    
    public boolean deactivateTrainee(String username, String password) {
        log.info("Deactivating trainee: {}", username);
        return toggleTraineeStatus(username, password, false);
    }
    
    private boolean toggleTraineeStatus(String username, String password, boolean isActive) {
        Optional<Trainee> trainee = authenticationService.getAuthenticatedTrainee(username, password);
        if (trainee.isPresent()) {
            if (trainee.get().getIsActive().equals(isActive)) {
                log.warn("Trainee {} is already {}", username, isActive ? "active" : "inactive");
                return false; // Not idempotent - return false if already in desired state
            }
            
            trainee.get().setIsActive(isActive);
            traineeRepository.save(trainee.get());
            log.info("Trainee {} status changed to: {}", username, isActive ? "active" : "inactive");
            return true;
        }
        
        log.warn("Failed to change trainee status for: {} - authentication failed", username);
        return false;
    }
    
    public boolean deleteTraineeByUsername(String username, String password) {
        log.info("Deleting trainee profile for username: {}", username);
        
        Optional<Trainee> trainee = authenticationService.getAuthenticatedTrainee(username, password);
        if (trainee.isPresent()) {
            // Cascade delete trainings
            trainingRepository.deleteByTraineeUsername(username);
            traineeRepository.deleteByUsername(username);
            log.info("Deleted trainee profile and related trainings for username: {}", username);
            return true;
        }
        
        log.warn("Failed to delete trainee: {} - authentication failed", username);
        return false;
    }
    
    public Trainee updateTraineeTrainersList(String traineeUsername, String traineePassword, 
                                           List<String> trainerUsernames) {
        log.info("Updating trainers list for trainee: {}", traineeUsername);
        
        Optional<Trainee> trainee = authenticationService.getAuthenticatedTrainee(traineeUsername, traineePassword);
        if (trainee.isEmpty()) {
            throw new SecurityException("Authentication failed for trainee: " + traineeUsername);
        }
        
        Trainee existingTrainee = trainee.get();
        existingTrainee.getTrainers().clear();
        
        for (String trainerUsername : trainerUsernames) {
            Optional<Trainer> trainer = trainerRepository.findByUsername(trainerUsername);
            if (trainer.isPresent()) {
                existingTrainee.getTrainers().add(trainer.get());
                log.debug("Added trainer {} to trainee {}", trainerUsername, traineeUsername);
            } else {
                log.warn("Trainer not found with username: {}", trainerUsername);
            }
        }
        
        Trainee savedTrainee = traineeRepository.save(existingTrainee);
        log.info("Updated trainers list for trainee: {} with {} trainers", 
                traineeUsername, savedTrainee.getTrainers().size());
        return savedTrainee;
    }
    
    @Transactional(readOnly = true)
    public List<Trainee> selectAllTrainees() {
        log.debug("Selecting all trainees");
        return traineeRepository.findAll();
    }
} 