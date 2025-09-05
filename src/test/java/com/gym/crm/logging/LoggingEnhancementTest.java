package com.gym.crm.logging;

import com.gym.crm.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class LoggingEnhancementTest {

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Test
    void testAuditServiceIsConfigured() {
        assertNotNull(auditService);
        assertNotNull(auditEventRepository);
    }

    @Test
    void testAuthenticationAuditLogging() {
        String username = "testuser";
        String source = "web";

        // Test successful authentication
        auditService.logAuthentication(username, true, source);

        // Verify audit event was recorded
        List<AuditEvent> events = auditEventRepository.find(username, Instant.now().minusSeconds(10), "AUTHENTICATION_SUCCESS");
        assertFalse(events.isEmpty());
        
        AuditEvent event = events.get(0);
        assertEquals("AUTHENTICATION_SUCCESS", event.getType());
        assertEquals(username, event.getPrincipal());
        assertTrue(event.getData().containsKey("source"));
        assertEquals(source, event.getData().get("source"));
        assertEquals(true, event.getData().get("success"));
    }

    @Test
    void testUserRegistrationAuditLogging() {
        String username = "newuser";
        String userType = "TRAINEE";

        auditService.logUserRegistration(username, userType);

        List<AuditEvent> events = auditEventRepository.find(username, Instant.now().minusSeconds(10), "USER_REGISTRATION");
        assertFalse(events.isEmpty());
        
        AuditEvent event = events.get(0);
        assertEquals("USER_REGISTRATION", event.getType());
        assertEquals(username, event.getPrincipal());
        assertEquals(userType, event.getData().get("userType"));
    }

    @Test
    void testProfileUpdateAuditLogging() {
        String username = "updateuser";
        String userType = "TRAINER";
        Map<String, Object> changes = new HashMap<>();
        changes.put("firstName", "NewFirstName");
        changes.put("isActive", true);

        auditService.logProfileUpdate(username, userType, changes);

        List<AuditEvent> events = auditEventRepository.find(username, Instant.now().minusSeconds(10), "PROFILE_UPDATE");
        assertFalse(events.isEmpty());
        
        AuditEvent event = events.get(0);
        assertEquals("PROFILE_UPDATE", event.getType());
        assertEquals(username, event.getPrincipal());
        assertEquals(userType, event.getData().get("userType"));
        assertNotNull(event.getData().get("changes"));
    }

    @Test
    void testPasswordChangeAuditLogging() {
        String username = "passworduser";

        auditService.logPasswordChange(username);

        List<AuditEvent> events = auditEventRepository.find(username, Instant.now().minusSeconds(10), "PASSWORD_CHANGE");
        assertFalse(events.isEmpty());
        
        AuditEvent event = events.get(0);
        assertEquals("PASSWORD_CHANGE", event.getType());
        assertEquals(username, event.getPrincipal());
    }

    @Test
    void testTrainingCreationAuditLogging() {
        String traineeUsername = "trainee1";
        String trainerUsername = "trainer1";
        String trainingName = "Morning Workout";

        auditService.logTrainingCreation(traineeUsername, trainerUsername, trainingName);

        List<AuditEvent> events = auditEventRepository.find("system", Instant.now().minusSeconds(10), "TRAINING_CREATED");
        assertFalse(events.isEmpty());
        
        AuditEvent event = events.get(0);
        assertEquals("TRAINING_CREATED", event.getType());
        assertEquals("system", event.getPrincipal());
        assertEquals(traineeUsername, event.getData().get("trainee"));
        assertEquals(trainerUsername, event.getData().get("trainer"));
        assertEquals(trainingName, event.getData().get("trainingName"));
    }

    @Test
    void testAccountStatusChangeAuditLogging() {
        String username = "statususer";
        String userType = "TRAINEE";

        // Test activation
        auditService.logAccountStatusChange(username, userType, true);

        List<AuditEvent> activationEvents = auditEventRepository.find(username, Instant.now().minusSeconds(10), "ACCOUNT_ACTIVATED");
        assertFalse(activationEvents.isEmpty());
        
        AuditEvent activationEvent = activationEvents.get(0);
        assertEquals("ACCOUNT_ACTIVATED", activationEvent.getType());
        assertEquals("ACTIVE", activationEvent.getData().get("newStatus"));

        // Test deactivation
        auditService.logAccountStatusChange(username, userType, false);

        List<AuditEvent> deactivationEvents = auditEventRepository.find(username, Instant.now().minusSeconds(10), "ACCOUNT_DEACTIVATED");
        assertFalse(deactivationEvents.isEmpty());
        
        AuditEvent deactivationEvent = deactivationEvents.get(0);
        assertEquals("ACCOUNT_DEACTIVATED", deactivationEvent.getType());
        assertEquals("INACTIVE", deactivationEvent.getData().get("newStatus"));
    }

    @Test
    void testAccountDeletionAuditLogging() {
        String username = "deleteuser";
        String userType = "TRAINER";

        auditService.logAccountDeletion(username, userType);

        List<AuditEvent> events = auditEventRepository.find(username, Instant.now().minusSeconds(10), "ACCOUNT_DELETED");
        assertFalse(events.isEmpty());
        
        AuditEvent event = events.get(0);
        assertEquals("ACCOUNT_DELETED", event.getType());
        assertEquals(username, event.getPrincipal());
        assertEquals(userType, event.getData().get("userType"));
    }
}


