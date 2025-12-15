package com.gym.crm.security;

import com.gym.crm.model.User;
import com.gym.crm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BruteForceProtectionTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BruteForceProtectionService bruteForceProtectionService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setFailedLoginAttempts(0);
        testUser.setAccountLockedUntil(null);
    }

    @Test
    void testRecordFailedLogin() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        bruteForceProtectionService.recordFailedLogin("testuser");

        assertEquals(1, testUser.getFailedLoginAttempts());
        assertNull(testUser.getAccountLockedUntil());
        verify(userRepository).save(testUser);
    }

    @Test
    void testAccountLockedAfterThreeFailedAttempts() {
        testUser.setFailedLoginAttempts(2);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        bruteForceProtectionService.recordFailedLogin("testuser");

        assertEquals(3, testUser.getFailedLoginAttempts());
        assertNotNull(testUser.getAccountLockedUntil());
        assertTrue(testUser.getAccountLockedUntil().isAfter(LocalDateTime.now()));
        verify(userRepository).save(testUser);
    }

    @Test
    void testIsAccountLocked_WhenNotLocked() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        boolean isLocked = bruteForceProtectionService.isAccountLocked("testuser");

        assertFalse(isLocked);
    }

    @Test
    void testIsAccountLocked_WhenLocked() {
        testUser.setAccountLockedUntil(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        boolean isLocked = bruteForceProtectionService.isAccountLocked("testuser");

        assertTrue(isLocked);
    }

    @Test
    void testIsAccountLocked_WhenLockExpired() {
        testUser.setAccountLockedUntil(LocalDateTime.now().minusMinutes(1));
        testUser.setFailedLoginAttempts(3);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        boolean isLocked = bruteForceProtectionService.isAccountLocked("testuser");

        assertFalse(isLocked);
        // Verify that unlock method was called internally
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    @Test
    void testResetFailedAttempts() {
        testUser.setFailedLoginAttempts(2);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        bruteForceProtectionService.resetFailedAttempts("testuser");

        assertEquals(0, testUser.getFailedLoginAttempts());
        assertNull(testUser.getAccountLockedUntil());
        verify(userRepository).save(testUser);
    }

    @Test
    void testUnlockAccount() {
        testUser.setFailedLoginAttempts(3);
        testUser.setAccountLockedUntil(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        bruteForceProtectionService.unlockAccount("testuser");

        assertEquals(0, testUser.getFailedLoginAttempts());
        assertNull(testUser.getAccountLockedUntil());
        verify(userRepository).save(testUser);
    }

    @Test
    void testGetFailedAttempts() {
        testUser.setFailedLoginAttempts(2);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        int attempts = bruteForceProtectionService.getFailedAttempts("testuser");

        assertEquals(2, attempts);
    }

    @Test
    void testGetAccountLockoutTime() {
        LocalDateTime lockoutTime = LocalDateTime.now().plusMinutes(5);
        testUser.setAccountLockedUntil(lockoutTime);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        LocalDateTime result = bruteForceProtectionService.getAccountLockoutTime("testuser");

        assertEquals(lockoutTime, result);
    }

    @Test
    void testRecordFailedLogin_NonExistentUser() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Should not throw exception
        assertDoesNotThrow(() -> bruteForceProtectionService.recordFailedLogin("nonexistent"));

        verify(userRepository, never()).save(any());
    }
}



