package com.gym.crm.controller;

import com.gym.crm.dto.request.*;
import com.gym.crm.dto.response.*;
import com.gym.crm.exception.UserNotFoundException;
import com.gym.crm.facade.GymCrmFacade;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.util.TransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trainers")
@Tag(name = "Trainer Management", description = "APIs for managing trainer profiles and operations")
@Slf4j
public class TrainerController {
    
    @Autowired
    private GymCrmFacade gymCrmFacade;
    
    @Operation(summary = "Register a new trainer", description = "Creates a new trainer profile with auto-generated credentials")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trainer registered successfully",
                content = @Content(schema = @Schema(implementation = RegistrationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> registerTrainer(
            @Valid @RequestBody TrainerRegistrationRequest request) {
        
        String transactionId = TransactionContext.getTransactionId();
        log.info("Registering trainer [{}]: {} {}", transactionId, request.getFirstName(), request.getLastName());
        
        // Find training type by ID
        List<TrainingType> trainingTypes = gymCrmFacade.getAllTrainingTypes();
        TrainingType specialization = trainingTypes.stream()
                .filter(type -> type.getId().equals(request.getSpecializationId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid specialization ID: " + request.getSpecializationId()));
        
        Trainer trainer = new Trainer(request.getFirstName(), request.getLastName(), specialization);
        Trainer createdTrainer = gymCrmFacade.createTrainer(trainer);
        
        RegistrationResponse response = new RegistrationResponse(
                createdTrainer.getUsername(), createdTrainer.getPassword());
        
        log.info("Trainer registered successfully [{}]: {}", transactionId, createdTrainer.getUsername());
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get trainer profile", description = "Retrieves trainer profile information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                content = @Content(schema = @Schema(implementation = TrainerProfileResponse.class))),
        @ApiResponse(responseCode = "401", description = "Authentication failed",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Trainer not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(
            @Parameter(description = "Trainer username") @PathVariable String username,
            @Parameter(description = "Password for authentication") @RequestParam String password) {
        
        String transactionId = TransactionContext.getTransactionId();
        log.info("Getting trainer profile [{}]: {}", transactionId, username);
        
        // Authenticate first
        if (!gymCrmFacade.matchTrainerCredentials(username, password)) {
            throw new SecurityException("Invalid credentials");
        }
        
        Optional<Trainer> trainerOpt = gymCrmFacade.getTrainerByUsername(username);
        if (trainerOpt.isEmpty()) {
            throw new UserNotFoundException("Trainer not found: " + username);
        }
        
        Trainer trainer = trainerOpt.get();
        List<TraineeSummaryDto> trainees = trainer.getTrainees().stream()
                .map(trainee -> new TraineeSummaryDto(
                        trainee.getUsername(),
                        trainee.getFirstName(),
                        trainee.getLastName()
                ))
                .collect(Collectors.toList());
        
        TrainerProfileResponse response = new TrainerProfileResponse(
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getSpecialization().getTrainingTypeName(),
                trainer.getIsActive(),
                trainees
        );
        
        log.info("Trainer profile retrieved [{}]: {}", transactionId, username);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Update trainer profile", description = "Updates trainer profile information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                content = @Content(schema = @Schema(implementation = TrainerUpdateResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Authentication failed",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping
    public ResponseEntity<TrainerUpdateResponse> updateTrainerProfile(
            @Valid @RequestBody TrainerUpdateRequest request,
            @Parameter(description = "Password for authentication") @RequestParam String password) {
        
        String transactionId = TransactionContext.getTransactionId();
        log.info("Updating trainer profile [{}]: {}", transactionId, request.getUsername());
        
        // Get existing trainer to preserve specialization (read-only)
        Optional<Trainer> existingTrainerOpt = gymCrmFacade.getTrainerByUsername(request.getUsername());
        if (existingTrainerOpt.isEmpty()) {
            throw new UserNotFoundException("Trainer not found: " + request.getUsername());
        }
        
        Trainer existingTrainer = existingTrainerOpt.get();
        Trainer updatedData = new Trainer(request.getFirstName(), request.getLastName(), 
                                        existingTrainer.getSpecialization());
        updatedData.setIsActive(request.getIsActive());
        
        Trainer updatedTrainer = gymCrmFacade.updateTrainer(request.getUsername(), password, updatedData);
        
        List<TraineeSummaryDto> trainees = updatedTrainer.getTrainees().stream()
                .map(trainee -> new TraineeSummaryDto(
                        trainee.getUsername(),
                        trainee.getFirstName(),
                        trainee.getLastName()
                ))
                .collect(Collectors.toList());
        
        TrainerUpdateResponse response = new TrainerUpdateResponse(
                updatedTrainer.getUsername(),
                updatedTrainer.getFirstName(),
                updatedTrainer.getLastName(),
                updatedTrainer.getSpecialization().getTrainingTypeName(),
                updatedTrainer.getIsActive(),
                trainees
        );
        
        log.info("Trainer profile updated [{}]: {}", transactionId, request.getUsername());
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get unassigned trainers", description = "Retrieves active trainers not assigned to a specific trainee")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trainers retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication failed",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/unassigned")
    public ResponseEntity<List<TrainerSummaryDto>> getUnassignedTrainers(
            @Parameter(description = "Trainee username") @RequestParam String traineeUsername,
            @Parameter(description = "Password for authentication") @RequestParam String password) {
        
        String transactionId = TransactionContext.getTransactionId();
        log.info("Getting unassigned trainers [{}] for trainee: {}", transactionId, traineeUsername);
        
        // Authenticate trainee first
        if (!gymCrmFacade.matchTraineeCredentials(traineeUsername, password)) {
            throw new SecurityException("Invalid credentials");
        }
        
        List<Trainer> unassignedTrainers = gymCrmFacade.getTrainersNotAssignedToTrainee(traineeUsername);
        
        List<TrainerSummaryDto> response = unassignedTrainers.stream()
                .filter(trainer -> trainer.getIsActive()) // Only active trainers
                .map(trainer -> new TrainerSummaryDto(
                        trainer.getUsername(),
                        trainer.getFirstName(),
                        trainer.getLastName(),
                        trainer.getSpecialization().getTrainingTypeName()
                ))
                .collect(Collectors.toList());
        
        log.info("Retrieved {} unassigned trainers [{}]", response.size(), transactionId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get trainer trainings", description = "Retrieves trainings for a trainer with optional filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trainings retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication failed",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingDto>> getTrainerTrainings(
            @Parameter(description = "Trainer username") @PathVariable String username,
            @Parameter(description = "Password for authentication") @RequestParam String password,
            @Parameter(description = "Period from date") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @Parameter(description = "Period to date") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @Parameter(description = "Trainee name filter") @RequestParam(required = false) String traineeName) {
        
        String transactionId = TransactionContext.getTransactionId();
        log.info("Getting trainer trainings [{}]: {} with filters", transactionId, username);
        
        List<Training> trainings = gymCrmFacade.getTrainerTrainingsList(
                username, password, periodFrom, periodTo, traineeName);
        
        List<TrainingDto> response = trainings.stream()
                .map(training -> new TrainingDto(
                        training.getTrainingName(),
                        training.getTrainingDate(),
                        training.getTrainingType().getTrainingTypeName(),
                        training.getTrainingDuration(),
                        null, // trainer name not needed for trainer's own trainings
                        training.getTrainee().getFirstName() + " " + training.getTrainee().getLastName()
                ))
                .collect(Collectors.toList());
        
        log.info("Retrieved {} trainings for trainer [{}]: {}", response.size(), transactionId, username);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Activate/Deactivate trainer", description = "Changes the active status of a trainer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data or already in desired state",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Authentication failed",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/activate")
    public ResponseEntity<Void> activateDeactivateTrainer(
            @Valid @RequestBody ActivationRequest request,
            @Parameter(description = "Password for authentication") @RequestParam String password) {
        
        String transactionId = TransactionContext.getTransactionId();
        log.info("Changing trainer activation status [{}]: {} to {}", 
                transactionId, request.getUsername(), request.getIsActive());
        
        boolean changed = request.getIsActive() 
                ? gymCrmFacade.activateTrainer(request.getUsername(), password)
                : gymCrmFacade.deactivateTrainer(request.getUsername(), password);
        
        if (!changed) {
            throw new IllegalArgumentException("User is already in the desired state or authentication failed");
        }
        
        log.info("Trainer activation status changed [{}]: {}", transactionId, request.getUsername());
        return ResponseEntity.ok().build();
    }
}
