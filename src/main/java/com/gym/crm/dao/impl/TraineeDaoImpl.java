package com.gym.crm.dao.impl;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.model.Trainee;
import com.gym.crm.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class TraineeDaoImpl implements TraineeDao {
    
    @Autowired
    private Storage<Trainee> traineeStorage;
    
    @Override
    public Trainee save(Trainee trainee) {
        log.debug("Saving trainee: {} {}", trainee.getFirstName(), trainee.getLastName());
        Long id = traineeStorage.save(trainee);
        trainee.setId(id);
        return trainee;
    }
    
    @Override
    public Optional<Trainee> findById(Long id) {
        log.debug("Finding trainee by id: {}", id);
        return traineeStorage.findById(id);
    }
    
    @Override
    public List<Trainee> findAll() {
        log.debug("Finding all trainees");
        return traineeStorage.findAll().values().stream().collect(Collectors.toList());
    }
    
    @Override
    public Trainee update(Trainee trainee) {
        log.debug("Updating trainee with id: {}", trainee.getId());
        traineeStorage.update(trainee.getId(), trainee);
        return trainee;
    }
    
    @Override
    public void delete(Long id) {
        log.debug("Deleting trainee with id: {}", id);
        traineeStorage.delete(id);
    }
    
    @Override
    public Optional<Trainee> findByUsername(String username) {
        log.debug("Finding trainee by username: {}", username);
        return traineeStorage.findAll().values().stream()
                .filter(trainee -> username.equals(trainee.getUsername()))
                .findFirst();
    }
    
    @Override
    public boolean existsByFirstNameAndLastName(String firstName, String lastName) {
        return traineeStorage.findAll().values().stream()
                .anyMatch(trainee -> firstName.equals(trainee.getFirstName()) && 
                         lastName.equals(trainee.getLastName()));
    }
} 