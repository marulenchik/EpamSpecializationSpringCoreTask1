package com.gym.crm.service;

import com.gym.crm.dao.TrainingDao;
import com.gym.crm.model.Training;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TrainingService {
    
    private TrainingDao trainingDao;

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    public Training createTraining(Training training) {
        log.info("Creating training session: {} for trainee {} and trainer {}", 
                training.getTrainingName(), training.getTraineeId(), training.getTrainerId());
        
        Training savedTraining = trainingDao.save(training);
        log.info("Created training with id: {}", savedTraining.getId());
        return savedTraining;
    }

    public Optional<Training> selectTraining(Long id) {
        log.debug("Selecting training with id: {}", id);
        return trainingDao.findById(id);
    }

    public List<Training> selectAllTrainings() {
        log.debug("Selecting all trainings");
        return trainingDao.findAll();
    }

    public List<Training> selectTrainingsByTraineeId(Long traineeId) {
        log.debug("Selecting trainings for trainee id: {}", traineeId);
        return trainingDao.findByTraineeId(traineeId);
    }

    public List<Training> selectTrainingsByTrainerId(Long trainerId) {
        log.debug("Selecting trainings for trainer id: {}", trainerId);
        return trainingDao.findByTrainerId(trainerId);
    }
} 