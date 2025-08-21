package com.gym.crm.service;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.User;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class AuthenticationService {
    
    private TrainerRepository trainerRepository;
    private TraineeRepository traineeRepository;
    
    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }
    
    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }
    
    public boolean authenticateTrainer(String username, String password) {
        log.debug("Authenticating trainer with username: {}", username);
        
        Optional<Trainer> trainer = trainerRepository.findByUsername(username);
        if (trainer.isPresent() && trainer.get().getPassword().equals(password)) {
            log.info("Trainer authentication successful for username: {}", username);
            return true;
        }
        
        log.warn("Trainer authentication failed for username: {}", username);
        return false;
    }
    
    public boolean authenticateTrainee(String username, String password) {
        log.debug("Authenticating trainee with username: {}", username);
        
        Optional<Trainee> trainee = traineeRepository.findByUsername(username);
        if (trainee.isPresent() && trainee.get().getPassword().equals(password)) {
            log.info("Trainee authentication successful for username: {}", username);
            return true;
        }
        
        log.warn("Trainee authentication failed for username: {}", username);
        return false;
    }
    
    public Optional<Trainer> getAuthenticatedTrainer(String username, String password) {
        if (authenticateTrainer(username, password)) {
            return trainerRepository.findByUsername(username);
        }
        return Optional.empty();
    }
    
    public Optional<Trainee> getAuthenticatedTrainee(String username, String password) {
        if (authenticateTrainee(username, password)) {
            return traineeRepository.findByUsername(username);
        }
        return Optional.empty();
    }
} 