package com.gym.crm.mongo.repository;

import com.gym.crm.mongo.model.TrainerTrainingSummary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainerTrainingSummaryRepository extends MongoRepository<TrainerTrainingSummary, String> {

    Optional<TrainerTrainingSummary> findByTrainerUsername(String trainerUsername);
}

