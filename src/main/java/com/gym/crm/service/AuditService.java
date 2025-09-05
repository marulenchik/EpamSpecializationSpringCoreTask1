package com.gym.crm.service;

import com.gym.crm.util.TransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for audit logging of security and business events
 */
@Service
@Slf4j
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    /**
     * Logs authentication events
     */
    public void logAuthentication(String username, boolean success, String source) {
        String transactionId = TransactionContext.getTransactionId();
        
        Map<String, Object> data = new HashMap<>();
        data.put("transactionId", transactionId);
        data.put("source", source);
        data.put("success", success);
        
        String eventType = success ? "AUTHENTICATION_SUCCESS" : "AUTHENTICATION_FAILURE";
        AuditEvent event = new AuditEvent(Instant.now(), username, eventType, data);
        
        auditEventRepository.add(event);
        
        if (success) {
            log.info("AUDIT_AUTH_SUCCESS [{}]: User '{}' authenticated successfully from {}", 
                    transactionId, username, source);
        } else {
            log.warn("AUDIT_AUTH_FAILURE [{}]: Authentication failed for user '{}' from {}", 
                    transactionId, username, source);
        }
    }

    /**
     * Logs user registration events
     */
    public void logUserRegistration(String username, String userType) {
        String transactionId = TransactionContext.getTransactionId();
        
        Map<String, Object> data = new HashMap<>();
        data.put("transactionId", transactionId);
        data.put("userType", userType);
        
        AuditEvent event = new AuditEvent(Instant.now(), username, "USER_REGISTRATION", data);
        auditEventRepository.add(event);
        
        log.info("AUDIT_USER_REGISTRATION [{}]: New {} registered: {}", 
                transactionId, userType, username);
    }

    /**
     * Logs profile update events
     */
    public void logProfileUpdate(String username, String userType, Map<String, Object> changes) {
        String transactionId = TransactionContext.getTransactionId();
        
        Map<String, Object> data = new HashMap<>();
        data.put("transactionId", transactionId);
        data.put("userType", userType);
        data.put("changes", changes);
        
        AuditEvent event = new AuditEvent(Instant.now(), username, "PROFILE_UPDATE", data);
        auditEventRepository.add(event);
        
        log.info("AUDIT_PROFILE_UPDATE [{}]: {} profile updated: {} - Changes: {}", 
                transactionId, userType, username, changes);
    }

    /**
     * Logs password change events
     */
    public void logPasswordChange(String username) {
        String transactionId = TransactionContext.getTransactionId();
        
        Map<String, Object> data = new HashMap<>();
        data.put("transactionId", transactionId);
        
        AuditEvent event = new AuditEvent(Instant.now(), username, "PASSWORD_CHANGE", data);
        auditEventRepository.add(event);
        
        log.info("AUDIT_PASSWORD_CHANGE [{}]: Password changed for user: {}", 
                transactionId, username);
    }

    /**
     * Logs training session creation
     */
    public void logTrainingCreation(String traineeUsername, String trainerUsername, String trainingName) {
        String transactionId = TransactionContext.getTransactionId();
        
        Map<String, Object> data = new HashMap<>();
        data.put("transactionId", transactionId);
        data.put("trainee", traineeUsername);
        data.put("trainer", trainerUsername);
        data.put("trainingName", trainingName);
        
        AuditEvent event = new AuditEvent(Instant.now(), "system", "TRAINING_CREATED", data);
        auditEventRepository.add(event);
        
        log.info("AUDIT_TRAINING_CREATED [{}]: Training '{}' created - Trainee: {}, Trainer: {}", 
                transactionId, trainingName, traineeUsername, trainerUsername);
    }

    /**
     * Logs account activation/deactivation events
     */
    public void logAccountStatusChange(String username, String userType, boolean isActive) {
        String transactionId = TransactionContext.getTransactionId();
        
        Map<String, Object> data = new HashMap<>();
        data.put("transactionId", transactionId);
        data.put("userType", userType);
        data.put("newStatus", isActive ? "ACTIVE" : "INACTIVE");
        
        String eventType = isActive ? "ACCOUNT_ACTIVATED" : "ACCOUNT_DEACTIVATED";
        AuditEvent event = new AuditEvent(Instant.now(), username, eventType, data);
        auditEventRepository.add(event);
        
        log.info("AUDIT_ACCOUNT_STATUS [{}]: {} account {} for user: {}", 
                transactionId, userType, isActive ? "activated" : "deactivated", username);
    }

    /**
     * Logs account deletion events
     */
    public void logAccountDeletion(String username, String userType) {
        String transactionId = TransactionContext.getTransactionId();
        
        Map<String, Object> data = new HashMap<>();
        data.put("transactionId", transactionId);
        data.put("userType", userType);
        
        AuditEvent event = new AuditEvent(Instant.now(), username, "ACCOUNT_DELETED", data);
        auditEventRepository.add(event);
        
        log.warn("AUDIT_ACCOUNT_DELETION [{}]: {} account deleted: {}", 
                transactionId, userType, username);
    }
}


