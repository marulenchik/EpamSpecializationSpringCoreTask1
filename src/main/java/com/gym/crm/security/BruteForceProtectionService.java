package com.gym.crm.security;

import com.gym.crm.model.User;
import com.gym.crm.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for handling brute force protection
 */
@Service
@Slf4j
public class BruteForceProtectionService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCKOUT_DURATION_MINUTES = 5;

    private final UserRepository userRepository;

    public BruteForceProtectionService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void recordFailedLogin(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            log.warn("Attempted login for non-existent user: {}", username);
            return;
        }

        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        
        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
            log.warn("Account locked for user: {} due to {} failed login attempts", 
                    username, user.getFailedLoginAttempts());
        }
        
        userRepository.save(user);
        log.info("Recorded failed login attempt #{} for user: {}", 
                user.getFailedLoginAttempts(), username);
    }

    @Transactional
    public void resetFailedAttempts(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return;
        }

        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            userRepository.save(user);
            log.info("Reset failed login attempts for user: {}", username);
        }
    }

    public boolean isAccountLocked(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return false;
        }

        if (user.getAccountLockedUntil() == null) {
            return false;
        }

        // Check if lockout period has expired
        if (LocalDateTime.now().isAfter(user.getAccountLockedUntil())) {
            // Unlock the account
            unlockAccount(username);
            return false;
        }

        return true;
    }

    @Transactional
    public void unlockAccount(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null && user.getAccountLockedUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            userRepository.save(user);
            log.info("Account unlocked for user: {}", username);
        }
    }

    public LocalDateTime getAccountLockoutTime(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        return user != null ? user.getAccountLockedUntil() : null;
    }

    public int getFailedAttempts(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        return user != null ? user.getFailedLoginAttempts() : 0;
    }
}



