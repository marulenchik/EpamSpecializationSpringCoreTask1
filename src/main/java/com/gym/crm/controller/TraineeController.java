package com.gym.crm.controller;

import com.gym.crm.dto.request.*;
import com.gym.crm.dto.response.*;
import com.gym.crm.exception.UserNotFoundException;
import com.gym.crm.facade.GymCrmFacade;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
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
@RequestMapping("/api/trainees")
@Tag(name = "Trainee Management", description = "APIs for managing trainee profiles and operations")
@Slf4j
public class TraineeController {
    
    @Autowired
    private GymCrmFacade gymCrmFacade;
    
    @Operation(summary = "Register a new trainee", description = "Creates a new trainee profile with auto-generated credentials")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trainee registered successfully",
                content = @Content(schema = @Schema(implementation = RegistrationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> registerTrainee(
            @Valid @RequestBody TraineeRegistrationRequest request) {
        
        String transactionId = TransactionContext.getTransactionId();
        log.info("Registering trainee [{}]: {} {}", transactionId, request.getFirstName(), request.getLastName());
        
        Trainee trainee = new Trainee(request.getFirstName(), request.getLastName(), 
                                    request.getDateOfBirth(), request.getAddress());
        Trainee createdTrainee = gymCrmFacade.createTrainee(trainee);
        
        RegistrationResponse response = new RegistrationResponse(
                createdTrainee.getUsername(), createdTrainee.getPassword());
        
        log.info("Trainee registered successfully [{}]: {}", transactionId, createdTrainee.getUsername());
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get trainee profile", description = "Retrieves trainee profile information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                content = @Content(schema = @Schema(implementation = TraineeProfileResponse.class))),
        @ApiResponse(responseCode = "401", description = "Authentication failed",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Trainee not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @Parameter(description = "Password for authentication") @RequestParam String password) {
        
        String transactionId = TransactionContext.getTransactionId();
        log.info("Getting trainee profile [{}]: {}", transactionId, username);
        
        // Authenticate first
        if (!gymCrmFacade.matchTraineeCredentials(username, password)) {
            throw new SecurityException("Invalid credentials");
        }
        
        Optional<Trainee> traineeOpt = gymCrmFacade.getTraineeByUsername(username);
        if (traineeOpt.isEmpty()) {
            throw new UserNotFoundException("Trainee not found: " + username);
        }
        
        Trainee trainee = traineeOpt.get();
        List<TrainerSummaryDto> trainers = trainee.getTrainers().stream()
                .map(trainer -> new TrainerSummaryDto(
                        trainer.getUsername(),
                        trainer.getFirstName(),
                        trainer.getLastName(),
                        trainer.getSpecialization().getTrainingTypeName()
                ))
                .collect(Collectors.toList());
        
        TraineeProfileResponse response = new TraineeProfileResponse(
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.getIsActive(),
                trainers
        );
        
        log.info("Trainee profile retrieved [{}]: {}", transactionId, username);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Update trainee profile", description = "Updates trainee profile information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                content = @Content(schema = @Schema(implementation = TraineeUpdateResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Authentication failed",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping
    public ResponseEntity<TraineeUpdateResponse> updateTraineeProfile(
            @Valid @RequestBody TraineeUpdateRequest request,
            @Parameter(description = "Password for authentication") @RequestParam String password) {
        
        String transactionId = TransactionContext.getTransactionId();
        log.info("Updating trainee profile [{}]: {}", transactionId, request.getUsername());
        
        Trainee updatedData = new Trainee(request.getFirstName(), request.getLastName(),
                                        request.getDateOfBirth(), request.getAddress());
        updatedData.setIsActive(request.getIsActive());
        
        Trainee updatedTrainee = gymCrmFacade.updateTrainee(request.getUsername(), password, updatedData);
        
        List<TrainerSummaryDto> trainers = updatedTrainee.getTrainers().stream()
                .map(trainer -> new TrainerSummaryDto(
                        trainer.getUsername(),
                        trainer.getFirstName(),
                        trainer.getLastName(),
                        trainer.getSpecialization().getTrainingTypeName()
                ))
                .collect(Collectors.toList());
        
        TraineeUpdateResponse response = new TraineeUpdateResponse(
                updatedTrainee.getUsername(),
                updatedTrainee.getFirstName(),
                updatedTrainee.getLastName(),
                updatedTrainee.getDateOfBirth(),
                updatedTrainee.getAddress(),
                updatedTrainee.getIsActive(),
                trainers
        );
        
        log.info("Trainee profile updated [{}]: {}", transactionId, request.getUsername());
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Delete trainee profile", description = "Deletes trainee profile and associated trainings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication failed",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Trainee not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteTraineeProfile(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @Parameter(description = "Password for authentication") @RequestParam String password) {
        
        String transactionId = TransactionContext.getTransactionId();
        log.info("Deleting trainee profile [{}]: {}", transactionId, username);
        
        boolean deleted = gymCrmFacade.deleteTraineeByUsername(username, password);
        if (!deleted) {
            throw new UserNotFoundException("Trainee not found or authentication failed: " + username);
        }
        
        log.info("Trainee profile deleted [{}]: {}", transactionId, username);
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "Get trainee trainings", description = "Retrieves trainings for a trainee with optional filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trainings retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication failed",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingDto>> getTraineeTrainings(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @Parameter(description = "Password for authentication") @RequestParam String password,
            @Parameter(description = "Period from date") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @Parameter(description = "Period to date") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @Parameter(description = "Trainer name filter") @RequestParam(required = false) String trainerName,
            @Parameter(description = "Training type filter") @RequestParam(required = false) String trainingType) {
        
        String transactionId = TransactionContext.getTransactionId();
        log.info("Getting trainee trainings [{}]: {} with filters", transactionId, username);
        
        List<Training> trainings = gymCrmFacade.getTraineeTrainingsList(
                username, password, periodFrom, periodTo, trainerName, trainingType);
        
        List<TrainingDto> response = trainings.stream()
                .map(training -> new TrainingDto(
                        training.getTrainingName(),
                        training.getTrainingDate(),
                        training.getTrainingType().getTrainingTypeName(),
                        training.getTrainingDuration(),
                        training.getTrainer().getFirstName() + " " + training.getTrainer().getLastName(),
                        null // trainee name not needed for trainee's own trainings
                ))
                .collect(Collectors.toList());
        
        log.info("Retrieved {} trainings for trainee [{}]: {}", response.size(), transactionId, username);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Update trainee's trainer list", description = "Updates the list of trainers assigned to a trainee")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trainer list updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Authentication failed",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/trainers")
    public ResponseEntity<List<TrainerSummaryDto>> updateTraineeTrainersList(
            @Valid @RequestBody UpdateTraineeTrainersRequest request,
            @Parameter(description = "Password for authentication") @RequestParam String password) {
        
        String transactionId = TransactionContext.getTransactionId();
        log.info("Updating trainee trainer list [{}]: {}", transactionId, request.getTraineeUsername());
        
        Trainee updatedTrainee = gymCrmFacade.updateTraineeTrainersList(
                request.getTraineeUsername(), password, request.getTrainerUsernames());
        
        List<TrainerSummaryDto> response = updatedTrainee.getTrainers().stream()
                .map(trainer -> new TrainerSummaryDto(
                        trainer.getUsername(),
                        trainer.getFirstName(),
                        trainer.getLastName(),
                        trainer.getSpecialization().getTrainingTypeName()
                ))
                .collect(Collectors.toList());
        
        log.info("Trainee trainer list updated [{}]: {} trainers", transactionId, response.size());
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Activate/Deactivate trainee", description = "Changes the active status of a trainee")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data or already in desired state",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Authentication failed",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/activate")
    public ResponseEntity<Void> activateDeactivateTrainee(
            @Valid @RequestBody ActivationRequest request,
            @Parameter(description = "Password for authentication") @RequestParam String password) {
        
        String transactionId = TransactionContext.getTransactionId();
        log.info("Changing trainee activation status [{}]: {} to {}", 
                transactionId, request.getUsername(), request.getIsActive());
        
        boolean changed = request.getIsActive() 
                ? gymCrmFacade.activateTrainee(request.getUsername(), password)
                : gymCrmFacade.deactivateTrainee(request.getUsername(), password);
        
        if (!changed) {
            throw new IllegalArgumentException("User is already in the desired state or authentication failed");
        }
        
        log.info("Trainee activation status changed [{}]: {}", transactionId, request.getUsername());
        return ResponseEntity.ok().build();
    }
}
