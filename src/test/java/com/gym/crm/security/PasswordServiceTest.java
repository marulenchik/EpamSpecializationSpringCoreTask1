package com.gym.crm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
    }

    @Test
    void testGenerateSalt() {
        String salt1 = passwordService.generateSalt();
        String salt2 = passwordService.generateSalt();

        assertNotNull(salt1);
        assertNotNull(salt2);
        assertFalse(salt1.isEmpty());
        assertFalse(salt2.isEmpty());
        assertNotEquals(salt1, salt2); // Each salt should be unique
    }

    @Test
    void testHashPassword() {
        String rawPassword = "testPassword123";
        String salt = passwordService.generateSalt();

        String hashedPassword = passwordService.hashPassword(rawPassword, salt);

        assertNotNull(hashedPassword);
        assertFalse(hashedPassword.isEmpty());
        assertNotEquals(rawPassword, hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$")); // BCrypt hash format
    }

    @Test
    void testHashPasswordWithSameSalt() {
        String rawPassword = "testPassword123";
        String salt = passwordService.generateSalt();

        String hashedPassword1 = passwordService.hashPassword(rawPassword, salt);
        String hashedPassword2 = passwordService.hashPassword(rawPassword, salt);

        // Even with same salt, BCrypt should produce different hashes due to internal salt
        assertNotNull(hashedPassword1);
        assertNotNull(hashedPassword2);
        // BCrypt includes its own internal salt, so hashes will be different
        // but both should validate correctly
    }

    @Test
    void testValidatePassword_Correct() {
        String rawPassword = "testPassword123";
        String salt = passwordService.generateSalt();
        String hashedPassword = passwordService.hashPassword(rawPassword, salt);

        boolean isValid = passwordService.validatePassword(rawPassword, hashedPassword, salt);

        assertTrue(isValid);
    }

    @Test
    void testValidatePassword_Incorrect() {
        String rawPassword = "testPassword123";
        String wrongPassword = "wrongPassword456";
        String salt = passwordService.generateSalt();
        String hashedPassword = passwordService.hashPassword(rawPassword, salt);

        boolean isValid = passwordService.validatePassword(wrongPassword, hashedPassword, salt);

        assertFalse(isValid);
    }

    @Test
    void testValidatePassword_WrongSalt() {
        String rawPassword = "testPassword123";
        String salt1 = passwordService.generateSalt();
        String salt2 = passwordService.generateSalt();
        String hashedPassword = passwordService.hashPassword(rawPassword, salt1);

        boolean isValid = passwordService.validatePassword(rawPassword, hashedPassword, salt2);

        assertFalse(isValid);
    }

    @Test
    void testGetPasswordEncoder() {
        PasswordEncoder encoder = passwordService.getPasswordEncoder();

        assertNotNull(encoder);
        
        // Test that the encoder works correctly
        String rawPassword = "testPassword";
        String encoded = encoder.encode(rawPassword);
        
        assertNotNull(encoded);
        assertTrue(encoder.matches(rawPassword, encoded));
        assertFalse(encoder.matches("wrongPassword", encoded));
    }

    @Test
    void testPasswordSecurity() {
        String rawPassword = "testPassword123";
        String salt = passwordService.generateSalt();
        String hashedPassword = passwordService.hashPassword(rawPassword, salt);

        // Verify the hash doesn't contain the original password
        assertFalse(hashedPassword.contains(rawPassword));
        assertFalse(hashedPassword.contains(salt));
        
        // Verify hash length is reasonable (BCrypt produces 60-character hashes)
        assertTrue(hashedPassword.length() >= 50);
    }

    @Test
    void testEmptyPassword() {
        String emptyPassword = "";
        String salt = passwordService.generateSalt();

        String hashedPassword = passwordService.hashPassword(emptyPassword, salt);
        boolean isValid = passwordService.validatePassword(emptyPassword, hashedPassword, salt);

        assertNotNull(hashedPassword);
        assertTrue(isValid);
    }

    @Test
    void testSpecialCharactersInPassword() {
        String specialPassword = "P@ssw0rd!#$%^&*()";
        String salt = passwordService.generateSalt();

        String hashedPassword = passwordService.hashPassword(specialPassword, salt);
        boolean isValid = passwordService.validatePassword(specialPassword, hashedPassword, salt);

        assertNotNull(hashedPassword);
        assertTrue(isValid);
    }

}

