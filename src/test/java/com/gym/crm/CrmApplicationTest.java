package com.gym.crm;

import com.gym.crm.facade.GymCrmFacade;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "logging.level.com.gym.crm=ERROR"
})
class CrmApplicationTest {
    
    @Autowired
    private GymCrmFacade gymCrmFacade;
    
    @Autowired
    private TraineeService traineeService;
    
    @Autowired
    private TrainerService trainerService;
    
    @Autowired
    private TrainingService trainingService;
    
    @Test
    void contextLoads() {
        assertNotNull(gymCrmFacade);
        assertNotNull(traineeService);
        assertNotNull(trainerService);
        assertNotNull(trainingService);
    }
    
    @Test
    void testTraineeWorkflow() {
        // Create trainee
        Trainee trainee = new Trainee("TestUser", "TestLast", LocalDate.of(1990, 1, 1), "Test Address");
        Trainee createdTrainee = gymCrmFacade.createTrainee(trainee);
        
        assertNotNull(createdTrainee);
        assertNotNull(createdTrainee.getId());
        assertNotNull(createdTrainee.getUsername());
        assertNotNull(createdTrainee.getPassword());
        assertTrue(createdTrainee.getIsActive());
        
        // Update trainee
        createdTrainee.setAddress("New Address");
        Trainee updatedTrainee = gymCrmFacade.updateTrainee(createdTrainee);
        assertEquals("New Address", updatedTrainee.getAddress());
        
        // Get trainee
        Optional<Trainee> foundTrainee = gymCrmFacade.getTrainee(createdTrainee.getId());
        assertTrue(foundTrainee.isPresent());
        assertEquals("New Address", foundTrainee.get().getAddress());
        
        // Delete trainee
        gymCrmFacade.deleteTrainee(createdTrainee.getId());
        Optional<Trainee> deletedTrainee = gymCrmFacade.getTrainee(createdTrainee.getId());
        assertTrue(deletedTrainee.isEmpty());
    }
    
    @Test
    void testTrainerWorkflow() {
        // Create trainer
        Trainer trainer = new Trainer("TestTrainer", "TestLast", "Test Specialization");
        Trainer createdTrainer = gymCrmFacade.createTrainer(trainer);
        
        assertNotNull(createdTrainer);
        assertNotNull(createdTrainer.getId());
        assertNotNull(createdTrainer.getUsername());
        assertNotNull(createdTrainer.getPassword());
        assertTrue(createdTrainer.getIsActive());
        
        // Update trainer
        createdTrainer.setSpecialization("New Specialization");
        Trainer updatedTrainer = gymCrmFacade.updateTrainer(createdTrainer);
        assertEquals("New Specialization", updatedTrainer.getSpecialization());
        
        // Get trainer
        Optional<Trainer> foundTrainer = gymCrmFacade.getTrainer(createdTrainer.getId());
        assertTrue(foundTrainer.isPresent());
        assertEquals("New Specialization", foundTrainer.get().getSpecialization());
    }
    
    @Test
    void testTrainingWorkflow() {
        // Create trainee and trainer first
        Trainee trainee = new Trainee("TrainingTestTrainee", "TestLast", LocalDate.of(1990, 1, 1), "Test Address");
        Trainee createdTrainee = gymCrmFacade.createTrainee(trainee);
        
        Trainer trainer = new Trainer("TrainingTestTrainer", "TestLast", "Test Specialization");
        Trainer createdTrainer = gymCrmFacade.createTrainer(trainer);
        
        // Create training
        Training training = new Training(createdTrainee.getId(), createdTrainer.getId(), 
                                       "Test Training", 1L, LocalDate.now(), 60);
        Training createdTraining = gymCrmFacade.createTraining(training);
        
        assertNotNull(createdTraining);
        assertNotNull(createdTraining.getId());
        assertEquals("Test Training", createdTraining.getTrainingName());
        
        // Get training
        Optional<Training> foundTraining = gymCrmFacade.getTraining(createdTraining.getId());
        assertTrue(foundTraining.isPresent());
        
        // Get trainings by trainee
        List<Training> traineeTrainings = gymCrmFacade.getTrainingsByTrainee(createdTrainee.getId());
        assertFalse(traineeTrainings.isEmpty());
        
        // Get trainings by trainer
        List<Training> trainerTrainings = gymCrmFacade.getTrainingsByTrainer(createdTrainer.getId());
        assertFalse(trainerTrainings.isEmpty());
    }
    
    @Test
    void testGetAllOperations() {
        List<Trainee> allTrainees = gymCrmFacade.getAllTrainees();
        assertNotNull(allTrainees);
        
        List<Trainer> allTrainers = gymCrmFacade.getAllTrainers();
        assertNotNull(allTrainers);
        
        List<Training> allTrainings = gymCrmFacade.getAllTrainings();
        assertNotNull(allTrainings);
    }
} 