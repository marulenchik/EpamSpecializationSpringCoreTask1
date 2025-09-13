package com.gym.crm.controller;

import com.gym.crm.dto.request.*;
import com.gym.crm.dto.response.*;
import com.gym.crm.facade.GymCrmFacade;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.security.JwtService;
import com.gym.crm.service.AuthenticationService;
import com.gym.crm.util.TransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Tag(name = "General Operations", description = "APIs for login, password management, training operations and training types")
@Slf4j
public class GymController {
    
    @Autowired
    private GymCrmFacade gymCrmFacade;
    
    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private JwtService jwtService;
    
    
    @Operation(summary = "User login", description = "Authenticates user credentials and returns JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
                content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String transactionId = TransactionContext.getTransactionId();
        log.info("User login attempt [{}]: {}", transactionId, request.getUsername());
        
        try {
            String token = authenticationService.authenticateAndGenerateToken(
                    request.getUsername(), 
                    request.getPassword()
            );
            
            // Determine user type for response
            String userType = determineUserType(request.getUsername());
            
            LoginResponse response = new LoginResponse(token, request.getUsername(), userType);
            log.info("User login successful [{}]: {}", transactionId, request.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.warn("User login failed [{}]: {} - {}", transactionId, request.getUsername(), e.getMessage());
            throw new SecurityException("Invalid credentials");
        }
    }
    
    @Operation(summary = "Change password", description = "Changes user password (both trainee and trainer)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid old password",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String transactionId = TransactionContext.getTransactionId();
        log.info("Password change request [{}]: {}", transactionId, request.getUsername());
        
        boolean traineePasswordChanged = gymCrmFacade.changeTraineePassword(
                request.getUsername(), request.getOldPassword(), request.getNewPassword());
        
        boolean trainerPasswordChanged = false;
        if (!traineePasswordChanged) {
            trainerPasswordChanged = gymCrmFacade.changeTrainerPassword(
                    request.getUsername(), request.getOldPassword(), request.getNewPassword());
        }
        
        if (!traineePasswordChanged && !trainerPasswordChanged) {
            throw new SecurityException("Invalid credentials or user not found");
        }
        
        log.info("Password changed successfully [{}]: {}", transactionId, request.getUsername());
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "Add training", description = "Adds a new training session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Training added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Trainee or trainer not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/trainings")
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequest request) {
        String transactionId = TransactionContext.getTransactionId();
        log.info("Adding training [{}]: {} between {} and {}", transactionId, request.getTrainingName(),
                request.getTraineeUsername(), request.getTrainerUsername());
        
        // Find trainee and trainer
        Optional<Trainee> traineeOpt = gymCrmFacade.getTraineeByUsername(request.getTraineeUsername());
        if (traineeOpt.isEmpty()) {
            throw new IllegalArgumentException("Trainee not found: " + request.getTraineeUsername());
        }
        
        Optional<Trainer> trainerOpt = gymCrmFacade.getTrainerByUsername(request.getTrainerUsername());
        if (trainerOpt.isEmpty()) {
            throw new IllegalArgumentException("Trainer not found: " + request.getTrainerUsername());
        }
        
        Trainee trainee = traineeOpt.get();
        Trainer trainer = trainerOpt.get();
        
        // Use trainer's specialization as training type
        Training training = new Training(
                trainee,
                trainer,
                request.getTrainingName(),
                trainer.getSpecialization(),
                request.getTrainingDate(),
                request.getTrainingDuration()
        );
        
        gymCrmFacade.addTraining(training);
        
        log.info("Training added successfully [{}]: {}", transactionId, request.getTrainingName());
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "Get training types", description = "Retrieves all available training types")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Training types retrieved successfully")
    })
    @GetMapping("/training-types")
    public ResponseEntity<List<TrainingTypeDto>> getTrainingTypes() {
        String transactionId = TransactionContext.getTransactionId();
        log.info("Getting training types [{}]", transactionId);
        
        List<TrainingType> trainingTypes = gymCrmFacade.getAllTrainingTypes();
        
        List<TrainingTypeDto> response = trainingTypes.stream()
                .map(type -> new TrainingTypeDto(type.getId(), type.getTrainingTypeName()))
                .collect(Collectors.toList());
        
        log.info("Retrieved {} training types [{}]", response.size(), transactionId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "User logout", description = "Logs out the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful")
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        String transactionId = TransactionContext.getTransactionId();
        log.info("User logout [{}]", transactionId);
        
        // JWT tokens are stateless, so logout is handled by client discarding the token
        // In a production environment, you might want to implement a token blacklist
        
        return ResponseEntity.ok("{\"message\":\"Logout successful\"}");
    }
    
    private String determineUserType(String username) {
        Optional<Trainer> trainer = gymCrmFacade.getTrainerByUsername(username);
        if (trainer.isPresent()) {
            return "TRAINER";
        }
        
        Optional<Trainee> trainee = gymCrmFacade.getTraineeByUsername(username);
        if (trainee.isPresent()) {
            return "TRAINEE";
        }
        
        return "USER";
    }
}
