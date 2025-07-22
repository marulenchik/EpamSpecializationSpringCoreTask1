package com.gym.crm.dao;

import com.gym.crm.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerDao {
    Trainer save(Trainer trainer);
    Optional<Trainer> findById(Long id);
    List<Trainer> findAll();
    Trainer update(Trainer trainer);
    Optional<Trainer> findByUsername(String username);
    boolean existsByFirstNameAndLastName(String firstName, String lastName);
} 