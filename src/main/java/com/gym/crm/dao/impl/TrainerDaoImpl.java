package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainerDao;
import com.gym.crm.model.Trainer;
import com.gym.crm.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class TrainerDaoImpl implements TrainerDao {
    
    @Autowired
    private Storage<Trainer> trainerStorage;
    
    @Override
    public Trainer save(Trainer trainer) {
        log.debug("Saving trainer: {} {}", trainer.getFirstName(), trainer.getLastName());
        Long id = trainerStorage.save(trainer);
        trainer.setId(id);
        return trainer;
    }
    
    @Override
    public Optional<Trainer> findById(Long id) {
        log.debug("Finding trainer by id: {}", id);
        return trainerStorage.findById(id);
    }
    
    @Override
    public List<Trainer> findAll() {
        log.debug("Finding all trainers");
        return trainerStorage.findAll().values().stream().collect(Collectors.toList());
    }
    
    @Override
    public Trainer update(Trainer trainer) {
        log.debug("Updating trainer with id: {}", trainer.getId());
        trainerStorage.update(trainer.getId(), trainer);
        return trainer;
    }
    
    @Override
    public Optional<Trainer> findByUsername(String username) {
        log.debug("Finding trainer by username: {}", username);
        return trainerStorage.findAll().values().stream()
                .filter(trainer -> username.equals(trainer.getUsername()))
                .findFirst();
    }
    
    @Override
    public boolean existsByFirstNameAndLastName(String firstName, String lastName) {
        return trainerStorage.findAll().values().stream()
                .anyMatch(trainer -> firstName.equals(trainer.getFirstName()) && 
                         lastName.equals(trainer.getLastName()));
    }
} 