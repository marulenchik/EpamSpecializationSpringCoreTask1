package com.gym.crm.dao;

import com.gym.crm.model.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeDao {
    Trainee save(Trainee trainee);
    Optional<Trainee> findById(Long id);
    List<Trainee> findAll();
    Trainee update(Trainee trainee);
    void delete(Long id);
    Optional<Trainee> findByUsername(String username);
    boolean existsByFirstNameAndLastName(String firstName, String lastName);
} 