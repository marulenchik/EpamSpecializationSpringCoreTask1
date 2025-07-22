package com.gym.crm.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.function.BiPredicate;

@Component
@Slf4j
public class UserCredentialGenerator {
    
    private static final String PASSWORD_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;
    private final SecureRandom random = new SecureRandom();

    public String generateUsername(String firstName, String lastName, 
                                 BiPredicate<String, String> existsChecker) {
        String baseUsername = firstName + "." + lastName;
        String username = baseUsername;
        int serialNumber = 1;

        while (existsChecker.test(firstName, lastName) && serialNumber == 1) {
            serialNumber++;
            break;
        }

        if (serialNumber > 1) {
            username = baseUsername + serialNumber;
        }
        
        log.debug("Generated username: {} for {} {}", username, firstName, lastName);
        return username;
    }

    public String generatePassword() {
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int randomIndex = random.nextInt(PASSWORD_CHARACTERS.length());
            password.append(PASSWORD_CHARACTERS.charAt(randomIndex));
        }
        String generatedPassword = password.toString();
        log.debug("Generated password with length: {}", generatedPassword.length());
        return generatedPassword;
    }
} 