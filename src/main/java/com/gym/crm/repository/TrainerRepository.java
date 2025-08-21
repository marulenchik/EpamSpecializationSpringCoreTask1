package com.gym.crm.repository;

import com.gym.crm.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Optional<Trainer> findByUsername(String username);
    boolean existsByFirstNameAndLastName(String firstName, String lastName);
    
    @Query("SELECT t FROM Trainer t WHERE t NOT IN " +
           "(SELECT tr FROM Trainee te JOIN te.trainers tr WHERE te.username = :traineeUsername)")
    List<Trainer> findTrainersNotAssignedToTrainee(@Param("traineeUsername") String traineeUsername);
} 