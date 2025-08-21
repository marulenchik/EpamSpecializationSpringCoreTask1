package com.gym.crm.integration;

import com.gym.crm.facade.GymCrmFacade;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.repository.TrainingTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "logging.level.com.gym.crm=ERROR",
        "spring.jpa.show-sql=false"
})
@Transactional
class GymCrmIntegrationTest {
    
    @Autowired
    private GymCrmFacade gymCrmFacade;
    
    @Autowired
    private TrainingTypeRepository trainingTypeRepository;
    
    @Test
    void testCompleteWorkflow() {
        // 1. Create Trainer
        TrainingType fitnessType = trainingTypeRepository.findByTrainingTypeName("Fitness").orElseThrow();
        Trainer trainer = new Trainer("John", "Smith", fitnessType);
        Trainer createdTrainer = gymCrmFacade.createTrainer(trainer);
        
        assertNotNull(createdTrainer.getId());
        assertNotNull(createdTrainer.getUsername());
        assertNotNull(createdTrainer.getPassword());
        assertTrue(createdTrainer.getIsActive());
        
        // 2. Create Trainee
        Trainee trainee = new Trainee("Alice", "Johnson", LocalDate.of(1990, 5, 15), "123 Main St");
        Trainee createdTrainee = gymCrmFacade.createTrainee(trainee);
        
        assertNotNull(createdTrainee.getId());
        assertNotNull(createdTrainee.getUsername());
        assertNotNull(createdTrainee.getPassword());
        assertTrue(createdTrainee.getIsActive());
        
        // 3. & 4. Test authentication
        assertTrue(gymCrmFacade.matchTrainerCredentials(createdTrainer.getUsername(), createdTrainer.getPassword()));
        assertTrue(gymCrmFacade.matchTraineeCredentials(createdTrainee.getUsername(), createdTrainee.getPassword()));
        assertFalse(gymCrmFacade.matchTrainerCredentials(createdTrainer.getUsername(), "wrongpassword"));
        
        // 5. & 6. Select by username
        Optional<Trainer> foundTrainer = gymCrmFacade.getTrainerByUsername(createdTrainer.getUsername());
        Optional<Trainee> foundTrainee = gymCrmFacade.getTraineeByUsername(createdTrainee.getUsername());
        
        assertTrue(foundTrainer.isPresent());
        assertTrue(foundTrainee.isPresent());
        assertEquals(createdTrainer.getId(), foundTrainer.get().getId());
        assertEquals(createdTrainee.getId(), foundTrainee.get().getId());
        
        // 7. & 8. Password change
        String newPassword = "newPassword123";
        assertTrue(gymCrmFacade.changeTrainerPassword(createdTrainer.getUsername(), 
                createdTrainer.getPassword(), newPassword));
        assertTrue(gymCrmFacade.changeTraineePassword(createdTrainee.getUsername(), 
                createdTrainee.getPassword(), newPassword));
        
        // Verify new passwords work
        assertTrue(gymCrmFacade.matchTrainerCredentials(createdTrainer.getUsername(), newPassword));
        assertTrue(gymCrmFacade.matchTraineeCredentials(createdTrainee.getUsername(), newPassword));
        
        // 9. & 10. Update profiles
        Trainer updatedTrainerData = new Trainer("John", "Doe", fitnessType);
        Trainer updatedTrainer = gymCrmFacade.updateTrainer(createdTrainer.getUsername(), 
                newPassword, updatedTrainerData);
        assertEquals("Doe", updatedTrainer.getLastName());
        
        Trainee updatedTraineeData = new Trainee("Alice", "Johnson", 
                LocalDate.of(1990, 5, 15), "456 Oak Ave");
        Trainee updatedTrainee = gymCrmFacade.updateTrainee(createdTrainee.getUsername(), 
                newPassword, updatedTraineeData);
        assertEquals("456 Oak Ave", updatedTrainee.getAddress());
        
        // 11. & 12. Activate/Deactivate
        assertTrue(gymCrmFacade.deactivateTrainer(createdTrainer.getUsername(), newPassword));
        assertFalse(gymCrmFacade.deactivateTrainer(createdTrainer.getUsername(), newPassword)); // Not idempotent
        assertTrue(gymCrmFacade.activateTrainer(createdTrainer.getUsername(), newPassword));
        
        assertTrue(gymCrmFacade.deactivateTrainee(createdTrainee.getUsername(), newPassword));
        assertFalse(gymCrmFacade.deactivateTrainee(createdTrainee.getUsername(), newPassword)); // Not idempotent
        assertTrue(gymCrmFacade.activateTrainee(createdTrainee.getUsername(), newPassword));
        
        // 16. Add training
        Training training = new Training(createdTrainee, createdTrainer, "Morning Workout", 
                fitnessType, LocalDate.now(), 60);
        Training addedTraining = gymCrmFacade.addTraining(training);
        
        assertNotNull(addedTraining.getId());
        assertEquals("Morning Workout", addedTraining.getTrainingName());
        
        // 14. Get trainee trainings with criteria
        List<Training> traineeTrainings = gymCrmFacade.getTraineeTrainingsList(
                createdTrainee.getUsername(), newPassword, null, null, null, null);
        assertFalse(traineeTrainings.isEmpty());
        
        // 15. Get trainer trainings with criteria
        List<Training> trainerTrainings = gymCrmFacade.getTrainerTrainingsList(
                createdTrainer.getUsername(), newPassword, null, null, null);
        assertFalse(trainerTrainings.isEmpty());
        
        // 17. Get trainers not assigned to trainee
        List<Trainer> unassignedTrainers = gymCrmFacade.getTrainersNotAssignedToTrainee(
                createdTrainee.getUsername());
        assertNotNull(unassignedTrainers);
        
        // 18. Update trainee's trainers list
        List<String> trainerUsernames = Arrays.asList(createdTrainer.getUsername());
        Trainee traineeWithTrainers = gymCrmFacade.updateTraineeTrainersList(
                createdTrainee.getUsername(), newPassword, trainerUsernames);
        assertEquals(1, traineeWithTrainers.getTrainers().size());
        
        // 13. Delete trainee (should cascade delete trainings)
        assertTrue(gymCrmFacade.deleteTraineeByUsername(createdTrainee.getUsername(), newPassword));
        
        // Verify trainee is deleted
        Optional<Trainee> deletedTrainee = gymCrmFacade.getTraineeByUsername(createdTrainee.getUsername());
        assertTrue(deletedTrainee.isEmpty());
    }
    
    @Test
    void testGetAllTrainingTypes() {
        List<TrainingType> trainingTypes = gymCrmFacade.getAllTrainingTypes();
        assertFalse(trainingTypes.isEmpty());
        assertTrue(trainingTypes.stream().anyMatch(type -> "Fitness".equals(type.getTrainingTypeName())));
    }
    
    @Test
    void testAuthenticationFailures() {
        // Test with non-existent users
        assertFalse(gymCrmFacade.matchTrainerCredentials("nonexistent", "password"));
        assertFalse(gymCrmFacade.matchTraineeCredentials("nonexistent", "password"));
        
        // Test password change with wrong old password
        TrainingType fitnessType = trainingTypeRepository.findByTrainingTypeName("Fitness").orElseThrow();
        Trainer trainer = gymCrmFacade.createTrainer(new Trainer("Test", "User", fitnessType));
        
        assertFalse(gymCrmFacade.changeTrainerPassword(trainer.getUsername(), "wrongpassword", "newpass"));
    }
} 