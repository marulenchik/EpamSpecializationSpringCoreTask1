package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainingDao;
import com.gym.crm.model.Training;
import com.gym.crm.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class TrainingDaoImpl implements TrainingDao {
    
    @Autowired
    private Storage<Training> trainingStorage;
    
    @Override
    public Training save(Training training) {
        log.debug("Saving training: {}", training.getTrainingName());
        Long id = trainingStorage.save(training);
        training.setId(id);
        return training;
    }
    
    @Override
    public Optional<Training> findById(Long id) {
        log.debug("Finding training by id: {}", id);
        return trainingStorage.findById(id);
    }
    
    @Override
    public List<Training> findAll() {
        log.debug("Finding all trainings");
        return trainingStorage.findAll().values().stream().collect(Collectors.toList());
    }
    
    @Override
    public List<Training> findByTraineeId(Long traineeId) {
        log.debug("Finding trainings by trainee id: {}", traineeId);
        return trainingStorage.findAll().values().stream()
                .filter(training -> traineeId.equals(training.getTraineeId()))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Training> findByTrainerId(Long trainerId) {
        log.debug("Finding trainings by trainer id: {}", trainerId);
        return trainingStorage.findAll().values().stream()
                .filter(training -> trainerId.equals(training.getTrainerId()))
                .collect(Collectors.toList());
    }
} 