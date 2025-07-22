package com.gym.crm.storage.impl;

import com.gym.crm.model.Trainer;
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
public class TrainerStorage implements Storage<Trainer> {
    
    private final Map<Long, Trainer> trainers = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Value("${storage.trainers.data.file:src/main/resources/data/trainers.txt}")
    private String dataFilePath;
    
    @PostConstruct
    public void initializeData() {
        log.info("Initializing trainer storage with data from: {}", dataFilePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    Trainer trainer = new Trainer(parts[0].trim(), parts[1].trim(), parts[2].trim());
                    save(trainer);
                }
            }
            log.info("Loaded {} trainers from data file", trainers.size());
        } catch (IOException e) {
            log.warn("Could not load trainers from file: {}", e.getMessage());
        }
    }
    
    @Override
    public Long save(Trainer trainer) {
        Long id = idGenerator.getAndIncrement();
        trainer.setId(id);
        trainers.put(id, trainer);
        log.debug("Saved trainer with id: {}", id);
        return id;
    }
    
    @Override
    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(trainers.get(id));
    }
    
    @Override
    public Map<Long, Trainer> findAll() {
        return new HashMap<>(trainers);
    }
    
    @Override
    public void update(Long id, Trainer trainer) {
        if (trainers.containsKey(id)) {
            trainer.setId(id);
            trainers.put(id, trainer);
            log.debug("Updated trainer with id: {}", id);
        }
    }
    
    @Override
    public void delete(Long id) {
        trainers.remove(id);
        log.debug("Deleted trainer with id: {}", id);
    }
    
    @Override
    public Long getNextId() {
        return idGenerator.get();
    }
} 