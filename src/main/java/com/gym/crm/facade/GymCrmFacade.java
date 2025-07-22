package com.gym.crm.facade;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

    public Trainee createTrainee(Trainee trainee) {
        log.info("Facade: Creating trainee");
        return traineeService.createTrainee(trainee);
    }
    
    public Trainee updateTrainee(Trainee trainee) {
        log.info("Facade: Updating trainee with id: {}", trainee.getId());
        return traineeService.updateTrainee(trainee);
    }
    
    public void deleteTrainee(Long id) {
        log.info("Facade: Deleting trainee with id: {}", id);
        traineeService.deleteTrainee(id);
    }
    
    public Optional<Trainee> getTrainee(Long id) {
        log.debug("Facade: Getting trainee with id: {}", id);
        return traineeService.selectTrainee(id);
    }
    
    public List<Trainee> getAllTrainees() {
        log.debug("Facade: Getting all trainees");
        return traineeService.selectAllTrainees();
    }

    public Trainer createTrainer(Trainer trainer) {
        log.info("Facade: Creating trainer");
        return trainerService.createTrainer(trainer);
    }
    
    public Trainer updateTrainer(Trainer trainer) {
        log.info("Facade: Updating trainer with id: {}", trainer.getId());
        return trainerService.updateTrainer(trainer);
    }
    
    public Optional<Trainer> getTrainer(Long id) {
        log.debug("Facade: Getting trainer with id: {}", id);
        return trainerService.selectTrainer(id);
    }
    
    public List<Trainer> getAllTrainers() {
        log.debug("Facade: Getting all trainers");
        return trainerService.selectAllTrainers();
    }

    public Training createTraining(Training training) {
        log.info("Facade: Creating training");
        return trainingService.createTraining(training);
    }
    
    public Optional<Training> getTraining(Long id) {
        log.debug("Facade: Getting training with id: {}", id);
        return trainingService.selectTraining(id);
    }
    
    public List<Training> getAllTrainings() {
        log.debug("Facade: Getting all trainings");
        return trainingService.selectAllTrainings();
    }
    
    public List<Training> getTrainingsByTrainee(Long traineeId) {
        log.debug("Facade: Getting trainings for trainee id: {}", traineeId);
        return trainingService.selectTrainingsByTraineeId(traineeId);
    }
    
    public List<Training> getTrainingsByTrainer(Long trainerId) {
        log.debug("Facade: Getting trainings for trainer id: {}", trainerId);
        return trainingService.selectTrainingsByTrainerId(trainerId);
    }
} 