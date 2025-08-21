package com.gym.crm.service;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    
    @Mock
    private TrainerRepository trainerRepository;
    
    @Mock
    private TraineeRepository traineeRepository;
    
    private AuthenticationService authenticationService;
    
    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService();
        authenticationService.setTrainerRepository(trainerRepository);
        authenticationService.setTraineeRepository(traineeRepository);
    }
    
    @Test
    void testAuthenticateTrainer_Success() {
        // Given
        String username = "john.doe";
        String password = "password123";
        Trainer trainer = new Trainer();
        trainer.setUsername(username);
        trainer.setPassword(password);
        
        when(trainerRepository.findByUsername(username)).thenReturn(Optional.of(trainer));
        
        // When
        boolean result = authenticationService.authenticateTrainer(username, password);
        
        // Then
        assertTrue(result);
        verify(trainerRepository).findByUsername(username);
    }
    
    @Test
    void testAuthenticateTrainer_WrongPassword() {
        // Given
        String username = "john.doe";
        String password = "password123";
        String wrongPassword = "wrongpassword";
        Trainer trainer = new Trainer();
        trainer.setUsername(username);
        trainer.setPassword(password);
        
        when(trainerRepository.findByUsername(username)).thenReturn(Optional.of(trainer));
        
        // When
        boolean result = authenticationService.authenticateTrainer(username, wrongPassword);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void testAuthenticateTrainer_UserNotFound() {
        // Given
        String username = "nonexistent";
        String password = "password123";
        
        when(trainerRepository.findByUsername(username)).thenReturn(Optional.empty());
        
        // When
        boolean result = authenticationService.authenticateTrainer(username, password);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void testAuthenticateTrainee_Success() {
        // Given
        String username = "alice.johnson";
        String password = "password123";
        Trainee trainee = new Trainee();
        trainee.setUsername(username);
        trainee.setPassword(password);
        
        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));
        
        // When
        boolean result = authenticationService.authenticateTrainee(username, password);
        
        // Then
        assertTrue(result);
        verify(traineeRepository).findByUsername(username);
    }
    
    @Test
    void testGetAuthenticatedTrainer_Success() {
        // Given
        String username = "john.doe";
        String password = "password123";
        Trainer trainer = new Trainer();
        trainer.setUsername(username);
        trainer.setPassword(password);
        
        when(trainerRepository.findByUsername(username)).thenReturn(Optional.of(trainer));
        
        // When
        Optional<Trainer> result = authenticationService.getAuthenticatedTrainer(username, password);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }
    
    @Test
    void testGetAuthenticatedTrainee_Success() {
        // Given
        String username = "alice.johnson";
        String password = "password123";
        Trainee trainee = new Trainee();
        trainee.setUsername(username);
        trainee.setPassword(password);
        
        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));
        
        // When
        Optional<Trainee> result = authenticationService.getAuthenticatedTrainee(username, password);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }
} 