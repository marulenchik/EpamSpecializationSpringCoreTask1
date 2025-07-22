package com.gym.crm.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.*;

class UserCredentialGeneratorTest {
    
    private UserCredentialGenerator credentialGenerator;
    
    @BeforeEach
    void setUp() {
        credentialGenerator = new UserCredentialGenerator();
    }
    
    @Test
    void testGenerateUsername_WhenNoExistingUser_ShouldReturnBasicUsername() {
        // Given
        String firstName = "John";
        String lastName = "Doe";
        BiPredicate<String, String> noExistingUser = (f, l) -> false;
        
        // When
        String username = credentialGenerator.generateUsername(firstName, lastName, noExistingUser);
        
        // Then
        assertEquals("John.Doe", username);
    }
    
    @Test
    void testGenerateUsername_WhenExistingUser_ShouldReturnUsernameWithSerialNumber() {
        // Given
        String firstName = "John";
        String lastName = "Doe";
        BiPredicate<String, String> existingUser = (f, l) -> true;
        
        // When
        String username = credentialGenerator.generateUsername(firstName, lastName, existingUser);
        
        // Then
        assertEquals("John.Doe2", username);
    }
    
    @Test
    void testGeneratePassword_ShouldReturnPasswordWithCorrectLength() {
        // When
        String password = credentialGenerator.generatePassword();
        
        // Then
        assertEquals(10, password.length());
    }
    
    @Test
    void testGeneratePassword_ShouldReturnAlphanumericPassword() {
        // When
        String password = credentialGenerator.generatePassword();
        
        // Then
        assertTrue(password.matches("[a-zA-Z0-9]+"));
    }
    
    @Test
    void testGeneratePassword_ShouldReturnDifferentPasswords() {
        // When
        String password1 = credentialGenerator.generatePassword();
        String password2 = credentialGenerator.generatePassword();
        
        // Then
        assertNotEquals(password1, password2);
    }
} 