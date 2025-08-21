package com.gym.crm.repository;

import com.gym.crm.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {
    
    @Query("SELECT t FROM Training t WHERE t.trainee.username = :traineeUsername " +
           "AND (:fromDate IS NULL OR t.trainingDate >= :fromDate) " +
           "AND (:toDate IS NULL OR t.trainingDate <= :toDate) " +
           "AND (:trainerName IS NULL OR CONCAT(t.trainer.firstName, ' ', t.trainer.lastName) LIKE %:trainerName%) " +
           "AND (:trainingTypeName IS NULL OR t.trainingType.trainingTypeName = :trainingTypeName)")
    List<Training> findTraineeTrainingsByCriteria(
        @Param("traineeUsername") String traineeUsername,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("trainerName") String trainerName,
        @Param("trainingTypeName") String trainingTypeName
    );
    
    @Query("SELECT t FROM Training t WHERE t.trainer.username = :trainerUsername " +
           "AND (:fromDate IS NULL OR t.trainingDate >= :fromDate) " +
           "AND (:toDate IS NULL OR t.trainingDate <= :toDate) " +
           "AND (:traineeName IS NULL OR CONCAT(t.trainee.firstName, ' ', t.trainee.lastName) LIKE %:traineeName%)")
    List<Training> findTrainerTrainingsByCriteria(
        @Param("trainerUsername") String trainerUsername,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("traineeName") String traineeName
    );
    
    void deleteByTraineeUsername(String traineeUsername);
} 