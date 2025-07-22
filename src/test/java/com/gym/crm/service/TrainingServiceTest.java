package com.gym.crm.service;

import com.gym.crm.dao.TrainingDao;
import com.gym.crm.model.Training;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {
    
    @Mock
    private TrainingDao trainingDao;
    
    private TrainingService trainingService;
    
    @BeforeEach
    void setUp() {
        trainingService = new TrainingService();
        trainingService.setTrainingDao(trainingDao);
    }
    
    @Test
    void testCreateTraining_ShouldSaveTraining() {
        // Given
        Training training = new Training(1L, 1L, "Morning Workout", 1L, LocalDate.now(), 60);
        Training savedTraining = new Training(1L, 1L, "Morning Workout", 1L, LocalDate.now(), 60);
        savedTraining.setId(1L);
        
        when(trainingDao.save(any(Training.class))).thenReturn(savedTraining);
        
        // When
        Training result = trainingService.createTraining(training);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Morning Workout", result.getTrainingName());
        verify(trainingDao).save(training);
    }
    
    @Test
    void testSelectTraining_ShouldReturnTraining() {
        // Given
        Long trainingId = 1L;
        Training training = new Training(1L, 1L, "Morning Workout", 1L, LocalDate.now(), 60);
        training.setId(trainingId);
        
        when(trainingDao.findById(trainingId)).thenReturn(Optional.of(training));
        
        // When
        Optional<Training> result = trainingService.selectTraining(trainingId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("Morning Workout", result.get().getTrainingName());
        verify(trainingDao).findById(trainingId);
    }
    
    @Test
    void testSelectAllTrainings_ShouldReturnAllTrainings() {
        // Given
        List<Training> trainings = Arrays.asList(
                new Training(1L, 1L, "Morning Workout", 1L, LocalDate.now(), 60),
                new Training(2L, 2L, "Evening Yoga", 2L, LocalDate.now(), 90)
        );
        
        when(trainingDao.findAll()).thenReturn(trainings);
        
        // When
        List<Training> result = trainingService.selectAllTrainings();
        
        // Then
        assertEquals(2, result.size());
        verify(trainingDao).findAll();
    }
    
    @Test
    void testSelectTrainingsByTraineeId_ShouldReturnTraineeTrainings() {
        // Given
        Long traineeId = 1L;
        List<Training> trainings = Arrays.asList(
                new Training(traineeId, 1L, "Morning Workout", 1L, LocalDate.now(), 60),
                new Training(traineeId, 2L, "Evening Yoga", 2L, LocalDate.now(), 90)
        );
        
        when(trainingDao.findByTraineeId(traineeId)).thenReturn(trainings);
        
        // When
        List<Training> result = trainingService.selectTrainingsByTraineeId(traineeId);
        
        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getTraineeId().equals(traineeId)));
        verify(trainingDao).findByTraineeId(traineeId);
    }
    
    @Test
    void testSelectTrainingsByTrainerId_ShouldReturnTrainerTrainings() {
        // Given
        Long trainerId = 1L;
        List<Training> trainings = Arrays.asList(
                new Training(1L, trainerId, "Morning Workout", 1L, LocalDate.now(), 60),
                new Training(2L, trainerId, "Afternoon Session", 3L, LocalDate.now(), 45)
        );
        
        when(trainingDao.findByTrainerId(trainerId)).thenReturn(trainings);
        
        // When
        List<Training> result = trainingService.selectTrainingsByTrainerId(trainerId);
        
        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getTrainerId().equals(trainerId)));
        verify(trainingDao).findByTrainerId(trainerId);
    }
} 