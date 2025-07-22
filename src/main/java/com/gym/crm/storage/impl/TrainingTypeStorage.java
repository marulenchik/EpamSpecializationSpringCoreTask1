package com.gym.crm.storage.impl;

import com.gym.crm.model.TrainingType;
import com.gym.crm.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class TrainingTypeStorage implements Storage<TrainingType> {
    
    private final Map<Long, TrainingType> trainingTypes = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Value("${storage.training.types.data.file:src/main/resources/data/training-types.txt}")
    private String dataFilePath;
    
    @PostConstruct
    public void initializeData() {
        log.info("Initializing training type storage with data from: {}", dataFilePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }
                TrainingType trainingType = new TrainingType(line.trim());
                save(trainingType);
            }
            log.info("Loaded {} training types from data file", trainingTypes.size());
        } catch (IOException e) {
            log.warn("Could not load training types from file: {}", e.getMessage());
        }
    }
    
    @Override
    public Long save(TrainingType trainingType) {
        Long id = idGenerator.getAndIncrement();
        trainingType.setId(id);
        trainingTypes.put(id, trainingType);
        log.debug("Saved training type with id: {}", id);
        return id;
    }
    
    @Override
    public Optional<TrainingType> findById(Long id) {
        return Optional.ofNullable(trainingTypes.get(id));
    }
    
    @Override
    public Map<Long, TrainingType> findAll() {
        return new HashMap<>(trainingTypes);
    }
    
    @Override
    public void update(Long id, TrainingType trainingType) {
        if (trainingTypes.containsKey(id)) {
            trainingType.setId(id);
            trainingTypes.put(id, trainingType);
            log.debug("Updated training type with id: {}", id);
        }
    }
    
    @Override
    public void delete(Long id) {
        trainingTypes.remove(id);
        log.debug("Deleted training type with id: {}", id);
    }
    
    @Override
    public Long getNextId() {
        return idGenerator.get();
    }
} 