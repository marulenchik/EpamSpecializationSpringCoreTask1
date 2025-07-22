package com.gym.crm.service;

import com.gym.crm.dao.TrainerDao;
import com.gym.crm.model.Trainer;
import com.gym.crm.util.UserCredentialGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TrainerService {
    
    private TrainerDao trainerDao;
    private UserCredentialGenerator credentialGenerator;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }
    
    @Autowired
    public void setCredentialGenerator(UserCredentialGenerator credentialGenerator) {
        this.credentialGenerator = credentialGenerator;
    }

    public Trainer createTrainer(Trainer trainer) {
        log.info("Creating trainer profile for {} {}", trainer.getFirstName(), trainer.getLastName());

        String username = credentialGenerator.generateUsername(
                trainer.getFirstName(), 
                trainer.getLastName(),
                trainerDao::existsByFirstNameAndLastName
        );
        String password = credentialGenerator.generatePassword();
        
        trainer.setUsername(username);
        trainer.setPassword(password);
        trainer.setIsActive(true);
        
        Trainer savedTrainer = trainerDao.save(trainer);
        log.info("Created trainer with id: {} and username: {}", savedTrainer.getId(), username);
        return savedTrainer;
    }

    public Trainer updateTrainer(Trainer trainer) {
        log.info("Updating trainer profile with id: {}", trainer.getId());
        
        Optional<Trainer> existingTrainer = trainerDao.findById(trainer.getId());
        if (existingTrainer.isEmpty()) {
            throw new IllegalArgumentException("Trainer not found with id: " + trainer.getId());
        }
        
        Trainer updatedTrainer = trainerDao.update(trainer);
        log.info("Updated trainer profile with id: {}", updatedTrainer.getId());
        return updatedTrainer;
    }

    public Optional<Trainer> selectTrainer(Long id) {
        log.debug("Selecting trainer with id: {}", id);
        return trainerDao.findById(id);
    }

    public Optional<Trainer> selectTrainerByUsername(String username) {
        log.debug("Selecting trainer with username: {}", username);
        return trainerDao.findByUsername(username);
    }

    public List<Trainer> selectAllTrainers() {
        log.debug("Selecting all trainers");
        return trainerDao.findAll();
    }
} 