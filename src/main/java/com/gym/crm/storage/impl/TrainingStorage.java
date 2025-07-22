package com.gym.crm.storage.impl;

import com.gym.crm.model.Training;
import com.gym.crm.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class TrainingStorage implements Storage<Training> {
    
    private final Map<Long, Training> trainings = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Value("${storage.trainings.data.file:src/main/resources/data/trainings.txt}")
    private String dataFilePath;
    
    @PostConstruct
    public void initializeData() {
        log.info("Initializing training storage with data from: {}", dataFilePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    Long traineeId = Long.parseLong(parts[0].trim());
                    Long trainerId = Long.parseLong(parts[1].trim());
                    String trainingName = parts[2].trim();
                    Long trainingTypeId = Long.parseLong(parts[3].trim());
                    LocalDate trainingDate = LocalDate.parse(parts[4].trim(), dateFormatter);
                    Integer duration = Integer.parseInt(parts[5].trim());
                    
                    Training training = new Training(traineeId, trainerId, trainingName, 
                                                   trainingTypeId, trainingDate, duration);
                    save(training);
                }
            }
            log.info("Loaded {} trainings from data file", trainings.size());
        } catch (IOException e) {
            log.warn("Could not load trainings from file: {}", e.getMessage());
        }
    }
    
    @Override
    public Long save(Training training) {
        Long id = idGenerator.getAndIncrement();
        training.setId(id);
        trainings.put(id, training);
        log.debug("Saved training with id: {}", id);
        return id;
    }
    
    @Override
    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(trainings.get(id));
    }
    
    @Override
    public Map<Long, Training> findAll() {
        return new HashMap<>(trainings);
    }
    
    @Override
    public void update(Long id, Training training) {
        if (trainings.containsKey(id)) {
            training.setId(id);
            trainings.put(id, training);
            log.debug("Updated training with id: {}", id);
        }
    }
    
    @Override
    public void delete(Long id) {
        trainings.remove(id);
        log.debug("Deleted training with id: {}", id);
    }
    
    @Override
    public Long getNextId() {
        return idGenerator.get();
    }
} 