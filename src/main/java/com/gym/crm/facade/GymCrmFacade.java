package com.gym.crm.facade;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class GymCrmFacade {
    
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymCrmFacade(TraineeService traineeService, 
                       TrainerService trainerService, 
                       TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        log.info("GymCrmFacade initialized with all services");
    }

    public Trainer createTrainer(Trainer trainer) {
        log.info("Facade: Creating trainer");
        return trainerService.createTrainer(trainer);
    }

    public Trainee createTrainee(Trainee trainee) {
        log.info("Facade: Creating trainee");
        return traineeService.createTrainee(trainee);
    }

    public boolean matchTraineeCredentials(String username, String password) {
        log.debug("Facade: Matching trainee credentials");
        return traineeService.matchTraineeCredentials(username, password);
    }

    public boolean matchTrainerCredentials(String username, String password) {
        log.debug("Facade: Matching trainer credentials");
        return trainerService.matchTrainerCredentials(username, password);
    }

    public Optional<Trainer> getTrainerByUsername(String username) {
        log.debug("Facade: Getting trainer by username: {}", username);
        return trainerService.selectTrainerByUsername(username);
    }

    public Optional<Trainee> getTraineeByUsername(String username) {
        log.debug("Facade: Getting trainee by username: {}", username);
        return traineeService.selectTraineeByUsername(username);
    }

    public boolean changeTraineePassword(String username, String oldPassword, String newPassword) {
        log.info("Facade: Changing trainee password");
        return traineeService.changeTraineePassword(username, oldPassword, newPassword);
    }

    public boolean changeTrainerPassword(String username, String oldPassword, String newPassword) {
        log.info("Facade: Changing trainer password");
        return trainerService.changeTrainerPassword(username, oldPassword, newPassword);
    }

    public Trainer updateTrainer(String username, String password, Trainer trainer) {
        log.info("Facade: Updating trainer profile");
        return trainerService.updateTrainer(username, password, trainer);
    }

    public Trainee updateTrainee(String username, String password, Trainee trainee) {
        log.info("Facade: Updating trainee profile");
        return traineeService.updateTrainee(username, password, trainee);
    }

    public boolean activateTrainee(String username, String password) {
        log.info("Facade: Activating trainee");
        return traineeService.activateTrainee(username, password);
    }
    
    public boolean deactivateTrainee(String username, String password) {
        log.info("Facade: Deactivating trainee");
        return traineeService.deactivateTrainee(username, password);
    }

    public boolean activateTrainer(String username, String password) {
        log.info("Facade: Activating trainer");
        return trainerService.activateTrainer(username, password);
    }
    
    public boolean deactivateTrainer(String username, String password) {
        log.info("Facade: Deactivating trainer");
        return trainerService.deactivateTrainer(username, password);
    }

    public boolean deleteTraineeByUsername(String username, String password) {
        log.info("Facade: Deleting trainee by username");
        return traineeService.deleteTraineeByUsername(username, password);
    }

    public List<Training> getTraineeTrainingsList(String traineeUsername, String traineePassword,
                                                 LocalDate fromDate, LocalDate toDate,
                                                 String trainerName, String trainingTypeName) {
        log.info("Facade: Getting trainee trainings list with criteria");
        return trainingService.getTraineeTrainingsList(traineeUsername, traineePassword,
                fromDate, toDate, trainerName, trainingTypeName);
    }

    public List<Training> getTrainerTrainingsList(String trainerUsername, String trainerPassword,
                                                 LocalDate fromDate, LocalDate toDate,
                                                 String traineeName) {
        log.info("Facade: Getting trainer trainings list with criteria");
        return trainingService.getTrainerTrainingsList(trainerUsername, trainerPassword,
                fromDate, toDate, traineeName);
    }
    
    // 16. Add training
    public Training addTraining(Training training) {
        log.info("Facade: Adding training");
        return trainingService.addTraining(training);
    }

    public List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername) {
        log.info("Facade: Getting trainers not assigned to trainee");
        return trainerService.getTrainersNotAssignedToTrainee(traineeUsername);
    }

    public Trainee updateTraineeTrainersList(String traineeUsername, String traineePassword,
                                            List<String> trainerUsernames) {
        log.info("Facade: Updating trainee's trainers list");
        return traineeService.updateTraineeTrainersList(traineeUsername, traineePassword, trainerUsernames);
    }

    public List<TrainingType> getAllTrainingTypes() {
        log.debug("Facade: Getting all training types");
        return trainingService.getAllTrainingTypes();
    }
    
    public List<Trainee> getAllTrainees() {
        log.debug("Facade: Getting all trainees");
        return traineeService.selectAllTrainees();
    }
    
    public List<Trainer> getAllTrainers() {
        log.debug("Facade: Getting all trainers");
        return trainerService.selectAllTrainers();
    }
    
    public List<Training> getAllTrainings() {
        log.debug("Facade: Getting all trainings");
        return trainingService.selectAllTrainings();
    }
} 