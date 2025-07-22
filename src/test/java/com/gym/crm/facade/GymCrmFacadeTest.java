package com.gym.crm.facade;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymCrmFacadeTest {
    
    @Mock
    private TraineeService traineeService;
    
    @Mock
    private TrainerService trainerService;
    
    @Mock
    private TrainingService trainingService;
    
    private GymCrmFacade facade;
    
    @BeforeEach
    void setUp() {
        facade = new GymCrmFacade(traineeService, trainerService, trainingService);
    }
    
    @Test
    void testCreateTrainee_ShouldCallTraineeService() {
        // Given
        Trainee trainee = new Trainee("Alice", "Johnson", LocalDate.of(1990, 5, 15), "123 Main St");
        Trainee savedTrainee = new Trainee("Alice", "Johnson", LocalDate.of(1990, 5, 15), "123 Main St");
        savedTrainee.setId(1L);
        
        when(traineeService.createTrainee(trainee)).thenReturn(savedTrainee);
        
        // When
        Trainee result = facade.createTrainee(trainee);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(traineeService).createTrainee(trainee);
    }
    
    @Test
    void testUpdateTrainee_ShouldCallTraineeService() {
        // Given
        Trainee trainee = new Trainee("Alice", "Johnson", LocalDate.of(1990, 5, 15), "456 Oak Ave");
        trainee.setId(1L);
        
        when(traineeService.updateTrainee(trainee)).thenReturn(trainee);
        
        // When
        Trainee result = facade.updateTrainee(trainee);
        
        // Then
        assertNotNull(result);
        assertEquals("456 Oak Ave", result.getAddress());
        verify(traineeService).updateTrainee(trainee);
    }
    
    @Test
    void testDeleteTrainee_ShouldCallTraineeService() {
        // Given
        Long traineeId = 1L;
        
        // When
        facade.deleteTrainee(traineeId);
        
        // Then
        verify(traineeService).deleteTrainee(traineeId);
    }
    
    @Test
    void testGetTrainee_ShouldCallTraineeService() {
        // Given
        Long traineeId = 1L;
        Trainee trainee = new Trainee("Alice", "Johnson", LocalDate.of(1990, 5, 15), "123 Main St");
        
        when(traineeService.selectTrainee(traineeId)).thenReturn(Optional.of(trainee));
        
        // When
        Optional<Trainee> result = facade.getTrainee(traineeId);
        
        // Then
        assertTrue(result.isPresent());
        verify(traineeService).selectTrainee(traineeId);
    }
    
    @Test
    void testCreateTrainer_ShouldCallTrainerService() {
        // Given
        Trainer trainer = new Trainer("John", "Doe", "Fitness");
        Trainer savedTrainer = new Trainer("John", "Doe", "Fitness");
        savedTrainer.setId(1L);
        
        when(trainerService.createTrainer(trainer)).thenReturn(savedTrainer);
        
        // When
        Trainer result = facade.createTrainer(trainer);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(trainerService).createTrainer(trainer);
    }
    
    @Test
    void testCreateTraining_ShouldCallTrainingService() {
        // Given
        Training training = new Training(1L, 1L, "Morning Workout", 1L, LocalDate.now(), 60);
        Training savedTraining = new Training(1L, 1L, "Morning Workout", 1L, LocalDate.now(), 60);
        savedTraining.setId(1L);
        
        when(trainingService.createTraining(training)).thenReturn(savedTraining);
        
        // When
        Training result = facade.createTraining(training);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(trainingService).createTraining(training);
    }
    
    @Test
    void testGetAllTrainees_ShouldCallTraineeService() {
        // Given
        List<Trainee> trainees = Arrays.asList(
                new Trainee("Alice", "Johnson", LocalDate.of(1990, 5, 15), "123 Main St"),
                new Trainee("Bob", "Williams", LocalDate.of(1985, 8, 20), "456 Oak Ave")
        );
        
        when(traineeService.selectAllTrainees()).thenReturn(trainees);
        
        // When
        List<Trainee> result = facade.getAllTrainees();
        
        // Then
        assertEquals(2, result.size());
        verify(traineeService).selectAllTrainees();
    }
    
    @Test
    void testGetTrainingsByTrainee_ShouldCallTrainingService() {
        // Given
        Long traineeId = 1L;
        List<Training> trainings = Arrays.asList(
                new Training(traineeId, 1L, "Morning Workout", 1L, LocalDate.now(), 60)
        );
        
        when(trainingService.selectTrainingsByTraineeId(traineeId)).thenReturn(trainings);
        
        // When
        List<Training> result = facade.getTrainingsByTrainee(traineeId);
        
        // Then
        assertEquals(1, result.size());
        verify(trainingService).selectTrainingsByTraineeId(traineeId);
    }
    
    @Test
    void testGetTrainingsByTrainer_ShouldCallTrainingService() {
        // Given
        Long trainerId = 1L;
        List<Training> trainings = Arrays.asList(
                new Training(1L, trainerId, "Morning Workout", 1L, LocalDate.now(), 60)
        );
        
        when(trainingService.selectTrainingsByTrainerId(trainerId)).thenReturn(trainings);
        
        // When
        List<Training> result = facade.getTrainingsByTrainer(trainerId);
        
        // Then
        assertEquals(1, result.size());
        verify(trainingService).selectTrainingsByTrainerId(trainerId);
    }
} 