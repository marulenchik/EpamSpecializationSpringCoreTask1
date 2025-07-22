package com.gym.crm.service;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.model.Trainee;
import com.gym.crm.util.UserCredentialGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {
    
    @Mock
    private TraineeDao traineeDao;
    
    @Mock
    private UserCredentialGenerator credentialGenerator;
    
    private TraineeService traineeService;
    
    @BeforeEach
    void setUp() {
        traineeService = new TraineeService();
        traineeService.setTraineeDao(traineeDao);
        traineeService.setCredentialGenerator(credentialGenerator);
    }
    
    @Test
    void testCreateTrainee_ShouldGenerateCredentialsAndSaveTrainee() {
        // Given
        Trainee trainee = new Trainee("Alice", "Johnson", LocalDate.of(1990, 5, 15), "123 Main St");
        Trainee savedTrainee = new Trainee("Alice", "Johnson", LocalDate.of(1990, 5, 15), "123 Main St");
        savedTrainee.setId(1L);
        savedTrainee.setUsername("Alice.Johnson");
        savedTrainee.setPassword("randomPass");
        savedTrainee.setIsActive(true);
        
        when(credentialGenerator.generateUsername(eq("Alice"), eq("Johnson"), any())).thenReturn("Alice.Johnson");
        when(credentialGenerator.generatePassword()).thenReturn("randomPass");
        when(traineeDao.save(any(Trainee.class))).thenReturn(savedTrainee);
        
        // When
        Trainee result = traineeService.createTrainee(trainee);
        
        // Then
        assertNotNull(result);
        assertEquals("Alice.Johnson", result.getUsername());
        assertEquals("randomPass", result.getPassword());
        assertTrue(result.getIsActive());
        verify(traineeDao).save(trainee);
    }
    
    @Test
    void testUpdateTrainee_WhenTraineeExists_ShouldUpdateSuccessfully() {
        // Given
        Long traineeId = 1L;
        Trainee existingTrainee = new Trainee("Alice", "Johnson", LocalDate.of(1990, 5, 15), "123 Main St");
        existingTrainee.setId(traineeId);
        
        Trainee updatedTrainee = new Trainee("Alice", "Johnson", LocalDate.of(1990, 5, 15), "456 Oak Ave");
        updatedTrainee.setId(traineeId);
        
        when(traineeDao.findById(traineeId)).thenReturn(Optional.of(existingTrainee));
        when(traineeDao.update(updatedTrainee)).thenReturn(updatedTrainee);
        
        // When
        Trainee result = traineeService.updateTrainee(updatedTrainee);
        
        // Then
        assertNotNull(result);
        assertEquals("456 Oak Ave", result.getAddress());
        verify(traineeDao).update(updatedTrainee);
    }
    
    @Test
    void testUpdateTrainee_WhenTraineeNotExists_ShouldThrowException() {
        // Given
        Long traineeId = 1L;
        Trainee trainee = new Trainee("Alice", "Johnson", LocalDate.of(1990, 5, 15), "123 Main St");
        trainee.setId(traineeId);
        
        when(traineeDao.findById(traineeId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> traineeService.updateTrainee(trainee));
        verify(traineeDao, never()).update(any());
    }
    
    @Test
    void testDeleteTrainee_WhenTraineeExists_ShouldDeleteSuccessfully() {
        // Given
        Long traineeId = 1L;
        Trainee existingTrainee = new Trainee("Alice", "Johnson", LocalDate.of(1990, 5, 15), "123 Main St");
        existingTrainee.setId(traineeId);
        
        when(traineeDao.findById(traineeId)).thenReturn(Optional.of(existingTrainee));
        
        // When
        traineeService.deleteTrainee(traineeId);
        
        // Then
        verify(traineeDao).delete(traineeId);
    }
    
    @Test
    void testDeleteTrainee_WhenTraineeNotExists_ShouldThrowException() {
        // Given
        Long traineeId = 1L;
        
        when(traineeDao.findById(traineeId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> traineeService.deleteTrainee(traineeId));
        verify(traineeDao, never()).delete(any());
    }
    
    @Test
    void testSelectTrainee_ShouldReturnTrainee() {
        // Given
        Long traineeId = 1L;
        Trainee trainee = new Trainee("Alice", "Johnson", LocalDate.of(1990, 5, 15), "123 Main St");
        trainee.setId(traineeId);
        
        when(traineeDao.findById(traineeId)).thenReturn(Optional.of(trainee));
        
        // When
        Optional<Trainee> result = traineeService.selectTrainee(traineeId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getFirstName());
        verify(traineeDao).findById(traineeId);
    }
    
    @Test
    void testSelectTraineeByUsername_ShouldReturnTrainee() {
        // Given
        String username = "Alice.Johnson";
        Trainee trainee = new Trainee("Alice", "Johnson", LocalDate.of(1990, 5, 15), "123 Main St");
        trainee.setUsername(username);
        
        when(traineeDao.findByUsername(username)).thenReturn(Optional.of(trainee));
        
        // When
        Optional<Trainee> result = traineeService.selectTraineeByUsername(username);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
        verify(traineeDao).findByUsername(username);
    }
    
    @Test
    void testSelectAllTrainees_ShouldReturnAllTrainees() {
        // Given
        List<Trainee> trainees = Arrays.asList(
                new Trainee("Alice", "Johnson", LocalDate.of(1990, 5, 15), "123 Main St"),
                new Trainee("Bob", "Williams", LocalDate.of(1985, 8, 20), "456 Oak Ave")
        );
        
        when(traineeDao.findAll()).thenReturn(trainees);
        
        // When
        List<Trainee> result = traineeService.selectAllTrainees();
        
        // Then
        assertEquals(2, result.size());
        verify(traineeDao).findAll();
    }
} 