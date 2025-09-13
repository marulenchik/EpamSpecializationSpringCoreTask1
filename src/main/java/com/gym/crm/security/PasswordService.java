package com.gym.crm.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for handling password encryption, salt generation, and validation
 */
@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    public PasswordService() {
        this.passwordEncoder = new BCryptPasswordEncoder(12);
        this.secureRandom = new SecureRandom();
    }

    /**
     * Generates a random salt
     */
    public String generateSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a password with the provided salt using BCrypt
     */
    public String hashPassword(String rawPassword, String salt) {
        // BCrypt handles salt internally, but we store our own salt for additional security
        String saltedPassword = rawPassword + salt;
        return passwordEncoder.encode(saltedPassword);
    }

    /**
     * Validates a raw password against the stored hash and salt
     */
    public boolean validatePassword(String rawPassword, String hashedPassword, String salt) {
        String saltedPassword = rawPassword + salt;
        return passwordEncoder.matches(saltedPassword, hashedPassword);
    }

    /**
     * Gets the underlying password encoder for Spring Security
     */
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}

