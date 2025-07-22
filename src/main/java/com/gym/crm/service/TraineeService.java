package com.gym.crm.service;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.model.Trainee;
import com.gym.crm.util.UserCredentialGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TraineeService {
    
    private TraineeDao traineeDao;
    private UserCredentialGenerator credentialGenerator;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }
    
    @Autowired
    public void setCredentialGenerator(UserCredentialGenerator credentialGenerator) {
        this.credentialGenerator = credentialGenerator;
    }

    public Trainee createTrainee(Trainee trainee) {
        log.info("Creating trainee profile for {} {}", trainee.getFirstName(), trainee.getLastName());

        String username = credentialGenerator.generateUsername(
                trainee.getFirstName(), 
                trainee.getLastName(),
                traineeDao::existsByFirstNameAndLastName
        );
        String password = credentialGenerator.generatePassword();
        
        trainee.setUsername(username);
        trainee.setPassword(password);
        trainee.setIsActive(true);
        
        Trainee savedTrainee = traineeDao.save(trainee);
        log.info("Created trainee with id: {} and username: {}", savedTrainee.getId(), username);
        return savedTrainee;
    }

    public Trainee updateTrainee(Trainee trainee) {
        log.info("Updating trainee profile with id: {}", trainee.getId());
        
        Optional<Trainee> existingTrainee = traineeDao.findById(trainee.getId());
        if (existingTrainee.isEmpty()) {
            throw new IllegalArgumentException("Trainee not found with id: " + trainee.getId());
        }
        
        Trainee updatedTrainee = traineeDao.update(trainee);
        log.info("Updated trainee profile with id: {}", updatedTrainee.getId());
        return updatedTrainee;
    }

    public void deleteTrainee(Long id) {
        log.info("Deleting trainee with id: {}", id);
        
        Optional<Trainee> existingTrainee = traineeDao.findById(id);
        if (existingTrainee.isEmpty()) {
            throw new IllegalArgumentException("Trainee not found with id: " + id);
        }
        
        traineeDao.delete(id);
        log.info("Deleted trainee with id: {}", id);
    }

    public Optional<Trainee> selectTrainee(Long id) {
        log.debug("Selecting trainee with id: {}", id);
        return traineeDao.findById(id);
    }

    public Optional<Trainee> selectTraineeByUsername(String username) {
        log.debug("Selecting trainee with username: {}", username);
        return traineeDao.findByUsername(username);
    }

    public List<Trainee> selectAllTrainees() {
        log.debug("Selecting all trainees");
        return traineeDao.findAll();
    }
} 