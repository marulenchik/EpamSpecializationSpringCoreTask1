package com.gym.crm.service;

import com.gym.crm.model.TrainingType;
import com.gym.crm.repository.TrainingTypeRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@Transactional
public class DataInitializationService {
    
    private TrainingTypeRepository trainingTypeRepository;
    
    @Autowired
    public void setTrainingTypeRepository(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }
    
    @PostConstruct
    public void initializeData() {
        log.info("Initializing database with training types");
        initializeTrainingTypes();
    }
    
    private void initializeTrainingTypes() {
        List<String> trainingTypeNames = Arrays.asList(
            "Fitness", "Yoga", "CrossFit", "Pilates", "Cardio", 
            "Strength Training", "Weight Loss", "Muscle Building"
        );
        
        for (String typeName : trainingTypeNames) {
            if (trainingTypeRepository.findByTrainingTypeName(typeName).isEmpty()) {
                TrainingType trainingType = new TrainingType(typeName);
                trainingTypeRepository.save(trainingType);
                log.debug("Created training type: {}", typeName);
            }
        }
        
        log.info("Training types initialization completed. Total types: {}", 
                trainingTypeRepository.count());
    }
} 