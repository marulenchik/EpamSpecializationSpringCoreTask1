package com.gym.crm.service;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.User;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.repository.UserRepository;
import com.gym.crm.security.BruteForceProtectionService;
import com.gym.crm.security.JwtService;
import com.gym.crm.security.PasswordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class AuthenticationService {
    
    private TrainerRepository trainerRepository;
    private TraineeRepository traineeRepository;
    private UserRepository userRepository;
    private PasswordService passwordService;
    private JwtService jwtService;
    private BruteForceProtectionService bruteForceProtectionService;
    private AuthenticationManager authenticationManager;
    
    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }
    
    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }
    
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Autowired
    public void setPasswordService(PasswordService passwordService) {
        this.passwordService = passwordService;
    }
    
    @Autowired
    public void setJwtService(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    
    @Autowired
    public void setBruteForceProtectionService(BruteForceProtectionService bruteForceProtectionService) {
        this.bruteForceProtectionService = bruteForceProtectionService;
    }
    
    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
    
    
    /**
     * Authenticate user and return JWT token
     */
    public String authenticateAndGenerateToken(String username, String password) {
        log.debug("Authenticating user with username: {}", username);
        
        // Check if account is locked
        if (bruteForceProtectionService.isAccountLocked(username)) {
            log.warn("Authentication failed - account locked for username: {}", username);
            throw new RuntimeException("Account is temporarily locked due to multiple failed login attempts");
        }
        
        try {
            // Authenticate with Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            // Reset failed attempts on successful authentication
            bruteForceProtectionService.resetFailedAttempts(username);
            
            // Determine user type
            String userType = determineUserType(username);
            
            // Generate JWT token
            String token = jwtService.generateToken(username, userType);
            
            log.info("Authentication successful for username: {}", username);
            return token;
            
        } catch (AuthenticationException e) {
            // Record failed login attempt
            bruteForceProtectionService.recordFailedLogin(username);
            log.warn("Authentication failed for username: {}", username);
            throw new RuntimeException("Invalid credentials");
        }
    }
    
    public boolean authenticateTrainer(String username, String password) {
        log.debug("Authenticating trainer with username: {}", username);
        
        Optional<Trainer> trainer = trainerRepository.findByUsername(username);
        if (trainer.isPresent() && passwordService.validatePassword(password, trainer.get().getPassword(), trainer.get().getSalt())) {
            log.info("Trainer authentication successful for username: {}", username);
            return true;
        }
        
        log.warn("Trainer authentication failed for username: {}", username);
        return false;
    }
    
    public boolean authenticateTrainee(String username, String password) {
        log.debug("Authenticating trainee with username: {}", username);
        
        Optional<Trainee> trainee = traineeRepository.findByUsername(username);
        if (trainee.isPresent() && passwordService.validatePassword(password, trainee.get().getPassword(), trainee.get().getSalt())) {
            log.info("Trainee authentication successful for username: {}", username);
            return true;
        }
        
        log.warn("Trainee authentication failed for username: {}", username);
        return false;
    }
    
    public Optional<Trainer> getAuthenticatedTrainer(String username, String password) {
        if (authenticateTrainer(username, password)) {
            return trainerRepository.findByUsername(username);
        }
        return Optional.empty();
    }
    
    public Optional<Trainee> getAuthenticatedTrainee(String username, String password) {
        if (authenticateTrainee(username, password)) {
            return traineeRepository.findByUsername(username);
        }
        return Optional.empty();
    }
    
    private String determineUserType(String username) {
        Optional<Trainer> trainer = trainerRepository.findByUsername(username);
        if (trainer.isPresent()) {
            return "TRAINER";
        }
        
        Optional<Trainee> trainee = traineeRepository.findByUsername(username);
        if (trainee.isPresent()) {
            return "TRAINEE";
        }
        
        return "USER";
    }
} 