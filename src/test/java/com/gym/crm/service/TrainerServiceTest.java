package com.gym.crm.service;

import com.gym.crm.dao.TrainerDao;
import com.gym.crm.model.Trainer;
import com.gym.crm.util.UserCredentialGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {
    
    @Mock
    private TrainerDao trainerDao;
    
    @Mock
    private UserCredentialGenerator credentialGenerator;
    
    private TrainerService trainerService;
    
    @BeforeEach
    void setUp() {
        trainerService = new TrainerService();
        trainerService.setTrainerDao(trainerDao);
        trainerService.setCredentialGenerator(credentialGenerator);
    }
    
    @Test
    void testCreateTrainer_ShouldGenerateCredentialsAndSaveTrainer() {
        // Given
        Trainer trainer = new Trainer("John", "Doe", "Fitness");
        Trainer savedTrainer = new Trainer("John", "Doe", "Fitness");
        savedTrainer.setId(1L);
        savedTrainer.setUsername("John.Doe");
        savedTrainer.setPassword("randomPass");
        savedTrainer.setIsActive(true);
        
        when(credentialGenerator.generateUsername(eq("John"), eq("Doe"), any())).thenReturn("John.Doe");
        when(credentialGenerator.generatePassword()).thenReturn("randomPass");
        when(trainerDao.save(any(Trainer.class))).thenReturn(savedTrainer);
        
        // When
        Trainer result = trainerService.createTrainer(trainer);
        
        // Then
        assertNotNull(result);
        assertEquals("John.Doe", result.getUsername());
        assertEquals("randomPass", result.getPassword());
        assertTrue(result.getIsActive());
        verify(trainerDao).save(trainer);
    }
    
    @Test
    void testUpdateTrainer_WhenTrainerExists_ShouldUpdateSuccessfully() {
        // Given
        Long trainerId = 1L;
        Trainer existingTrainer = new Trainer("John", "Doe", "Fitness");
        existingTrainer.setId(trainerId);
        
        Trainer updatedTrainer = new Trainer("John", "Smith", "CrossFit");
        updatedTrainer.setId(trainerId);
        
        when(trainerDao.findById(trainerId)).thenReturn(Optional.of(existingTrainer));
        when(trainerDao.update(updatedTrainer)).thenReturn(updatedTrainer);
        
        // When
        Trainer result = trainerService.updateTrainer(updatedTrainer);
        
        // Then
        assertNotNull(result);
        assertEquals("Smith", result.getLastName());
        assertEquals("CrossFit", result.getSpecialization());
        verify(trainerDao).update(updatedTrainer);
    }
    
    @Test
    void testUpdateTrainer_WhenTrainerNotExists_ShouldThrowException() {
        // Given
        Long trainerId = 1L;
        Trainer trainer = new Trainer("John", "Doe", "Fitness");
        trainer.setId(trainerId);
        
        when(trainerDao.findById(trainerId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> trainerService.updateTrainer(trainer));
        verify(trainerDao, never()).update(any());
    }
    
    @Test
    void testSelectTrainer_ShouldReturnTrainer() {
        // Given
        Long trainerId = 1L;
        Trainer trainer = new Trainer("John", "Doe", "Fitness");
        trainer.setId(trainerId);
        
        when(trainerDao.findById(trainerId)).thenReturn(Optional.of(trainer));
        
        // When
        Optional<Trainer> result = trainerService.selectTrainer(trainerId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
        verify(trainerDao).findById(trainerId);
    }
    
    @Test
    void testSelectTrainerByUsername_ShouldReturnTrainer() {
        // Given
        String username = "John.Doe";
        Trainer trainer = new Trainer("John", "Doe", "Fitness");
        trainer.setUsername(username);
        
        when(trainerDao.findByUsername(username)).thenReturn(Optional.of(trainer));
        
        // When
        Optional<Trainer> result = trainerService.selectTrainerByUsername(username);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
        verify(trainerDao).findByUsername(username);
    }
    
    @Test
    void testSelectAllTrainers_ShouldReturnAllTrainers() {
        // Given
        List<Trainer> trainers = Arrays.asList(
                new Trainer("John", "Doe", "Fitness"),
                new Trainer("Jane", "Smith", "Yoga")
        );
        
        when(trainerDao.findAll()).thenReturn(trainers);
        
        // When
        List<Trainer> result = trainerService.selectAllTrainers();
        
        // Then
        assertEquals(2, result.size());
        verify(trainerDao).findAll();
    }
} 