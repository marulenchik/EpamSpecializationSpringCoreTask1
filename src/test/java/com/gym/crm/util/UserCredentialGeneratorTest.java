package com.gym.crm.util;

import com.gym.crm.security.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCredentialGeneratorTest {
    
    @Mock
    private PasswordService passwordService;
    
    private UserCredentialGenerator credentialGenerator;
    
    @BeforeEach
    void setUp() {
        credentialGenerator = new UserCredentialGenerator(passwordService);
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
    
    @Test
    void testGenerateSecurePassword_ShouldReturnPasswordInfo() {
        // Given
        String salt = "testSalt";
        String hashedPassword = "hashedPassword";
        when(passwordService.generateSalt()).thenReturn(salt);
        when(passwordService.hashPassword(anyString(), anyString())).thenReturn(hashedPassword);
        
        // When
        UserCredentialGenerator.PasswordInfo passwordInfo = credentialGenerator.generateSecurePassword();
        
        // Then
        assertNotNull(passwordInfo);
        assertNotNull(passwordInfo.getRawPassword());
        assertEquals(10, passwordInfo.getRawPassword().length());
        assertEquals(hashedPassword, passwordInfo.getHashedPassword());
        assertEquals(salt, passwordInfo.getSalt());
    }
} 