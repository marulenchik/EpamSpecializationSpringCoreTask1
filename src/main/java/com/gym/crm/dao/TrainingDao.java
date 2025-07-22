package com.gym.crm.dao;

import com.gym.crm.model.Training;

import java.util.List;
import java.util.Optional;

public interface TrainingDao {
    Training save(Training training);
    Optional<Training> findById(Long id);
    List<Training> findAll();
    List<Training> findByTraineeId(Long traineeId);
    List<Training> findByTrainerId(Long trainerId);
} 