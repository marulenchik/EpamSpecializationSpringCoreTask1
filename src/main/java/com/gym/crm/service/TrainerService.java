package com.gym.crm.service;

import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.repository.TrainingTypeRepository;
import com.gym.crm.util.UserCredentialGenerator;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Validated
@Transactional
public class TrainerService {
    
    private TrainerRepository trainerRepository;
    private TrainingTypeRepository trainingTypeRepository;
    private UserCredentialGenerator credentialGenerator;
    private AuthenticationService authenticationService;
    
    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }
    
    @Autowired
    public void setTrainingTypeRepository(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }
    
    @Autowired
    public void setCredentialGenerator(UserCredentialGenerator credentialGenerator) {
        this.credentialGenerator = credentialGenerator;
    }
    
    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    
    public Trainer createTrainer(@Valid Trainer trainer) {
        log.info("Creating trainer profile for {} {}", trainer.getFirstName(), trainer.getLastName());
        
        String username = credentialGenerator.generateUsername(
                trainer.getFirstName(), 
                trainer.getLastName(),
                trainerRepository::existsByFirstNameAndLastName
        );
        String password = credentialGenerator.generatePassword();
        
        trainer.setUsername(username);
        trainer.setPassword(password);
        trainer.setIsActive(true);
        
        Trainer savedTrainer = trainerRepository.save(trainer);
        log.info("Created trainer with id: {} and username: {}", savedTrainer.getId(), username);
        return savedTrainer;
    }
    
    public Optional<Trainer> selectTrainerByUsername(String username) {
        log.debug("Selecting trainer with username: {}", username);
        return trainerRepository.findByUsername(username);
    }
    
    public boolean matchTrainerCredentials(String username, String password) {
        log.debug("Matching trainer credentials for username: {}", username);
        return authenticationService.authenticateTrainer(username, password);
    }
    
    public boolean changeTrainerPassword(String username, String oldPassword, String newPassword) {
        log.info("Changing password for trainer: {}", username);
        
        Optional<Trainer> trainer = authenticationService.getAuthenticatedTrainer(username, oldPassword);
        if (trainer.isPresent()) {
            trainer.get().setPassword(newPassword);
            trainerRepository.save(trainer.get());
            log.info("Password changed successfully for trainer: {}", username);
            return true;
        }
        
        log.warn("Password change failed for trainer: {} - authentication failed", username);
        return false;
    }
    
    public Trainer updateTrainer(String username, String password, @Valid Trainer updatedTrainer) {
        log.info("Updating trainer profile for username: {}", username);
        
        Optional<Trainer> trainer = authenticationService.getAuthenticatedTrainer(username, password);
        if (trainer.isEmpty()) {
            throw new SecurityException("Authentication failed for trainer: " + username);
        }
        
        Trainer existingTrainer = trainer.get();
        existingTrainer.setFirstName(updatedTrainer.getFirstName());
        existingTrainer.setLastName(updatedTrainer.getLastName());
        existingTrainer.setSpecialization(updatedTrainer.getSpecialization());
        
        Trainer savedTrainer = trainerRepository.save(existingTrainer);
        log.info("Updated trainer profile for username: {}", username);
        return savedTrainer;
    }
    
    public boolean activateTrainer(String username, String password) {
        log.info("Activating trainer: {}", username);
        return toggleTrainerStatus(username, password, true);
    }
    
    public boolean deactivateTrainer(String username, String password) {
        log.info("Deactivating trainer: {}", username);
        return toggleTrainerStatus(username, password, false);
    }
    
    private boolean toggleTrainerStatus(String username, String password, boolean isActive) {
        Optional<Trainer> trainer = authenticationService.getAuthenticatedTrainer(username, password);
        if (trainer.isPresent()) {
            if (trainer.get().getIsActive().equals(isActive)) {
                log.warn("Trainer {} is already {}", username, isActive ? "active" : "inactive");
                return false; // Not idempotent - return false if already in desired state
            }
            
            trainer.get().setIsActive(isActive);
            trainerRepository.save(trainer.get());
            log.info("Trainer {} status changed to: {}", username, isActive ? "active" : "inactive");
            return true;
        }
        
        log.warn("Failed to change trainer status for: {} - authentication failed", username);
        return false;
    }
    
    public List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername) {
        log.debug("Getting trainers not assigned to trainee: {}", traineeUsername);
        return trainerRepository.findTrainersNotAssignedToTrainee(traineeUsername);
    }
    
    @Transactional(readOnly = true)
    public List<Trainer> selectAllTrainers() {
        log.debug("Selecting all trainers");
        return trainerRepository.findAll();
    }
} 