package com.gym.crm.storage.impl;

import com.gym.crm.model.Trainee;
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
public class TraineeStorage implements Storage<Trainee> {
    
    private final Map<Long, Trainee> trainees = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Value("${storage.trainees.data.file:src/main/resources/data/trainees.txt}")
    private String dataFilePath;
    
    @PostConstruct
    public void initializeData() {
        log.info("Initializing trainee storage with data from: {}", dataFilePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    LocalDate dateOfBirth = LocalDate.parse(parts[2].trim(), dateFormatter);
                    Trainee trainee = new Trainee(parts[0].trim(), parts[1].trim(), dateOfBirth, parts[3].trim());
                    save(trainee);
                }
            }
            log.info("Loaded {} trainees from data file", trainees.size());
        } catch (IOException e) {
            log.warn("Could not load trainees from file: {}", e.getMessage());
        }
    }
    
    @Override
    public Long save(Trainee trainee) {
        Long id = idGenerator.getAndIncrement();
        trainee.setId(id);
        trainees.put(id, trainee);
        log.debug("Saved trainee with id: {}", id);
        return id;
    }
    
    @Override
    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(trainees.get(id));
    }
    
    @Override
    public Map<Long, Trainee> findAll() {
        return new HashMap<>(trainees);
    }
    
    @Override
    public void update(Long id, Trainee trainee) {
        if (trainees.containsKey(id)) {
            trainee.setId(id);
            trainees.put(id, trainee);
            log.debug("Updated trainee with id: {}", id);
        }
    }
    
    @Override
    public void delete(Long id) {
        trainees.remove(id);
        log.debug("Deleted trainee with id: {}", id);
    }
    
    @Override
    public Long getNextId() {
        return idGenerator.get();
    }
} 