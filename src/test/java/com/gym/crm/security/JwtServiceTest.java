package com.gym.crm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", 
                "mySecretKeyForJWTTokenGenerationThatShouldBeAtLeast256BitsLongForHS256Algorithm");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L); // 24 hours
    }

    @Test
    void testGenerateToken() {
        String username = "testuser";
        String userType = "TRAINEE";

        String token = jwtService.generateToken(username, userType);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT should have 3 parts
    }

    @Test
    void testExtractUsername() {
        String username = "testuser";
        String userType = "TRAINEE";
        String token = jwtService.generateToken(username, userType);

        String extractedUsername = jwtService.extractUsername(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void testExtractUserType() {
        String username = "testuser";
        String userType = "TRAINER";
        String token = jwtService.generateToken(username, userType);

        String extractedUserType = jwtService.extractUserType(token);

        assertEquals(userType, extractedUserType);
    }

    @Test
    void testIsTokenValid_ValidToken() {
        String username = "testuser";
        String userType = "TRAINEE";
        String token = jwtService.generateToken(username, userType);

        boolean isValid = jwtService.isTokenValid(token, username);

        assertTrue(isValid);
    }

    @Test
    void testIsTokenValid_WrongUsername() {
        String username = "testuser";
        String userType = "TRAINEE";
        String token = jwtService.generateToken(username, userType);

        boolean isValid = jwtService.isTokenValid(token, "wronguser");

        assertFalse(isValid);
    }

    @Test
    void testIsTokenValid_ExpiredToken() {
        // Create JWT service with very short expiration
        JwtService shortExpirationJwtService = new JwtService();
        ReflectionTestUtils.setField(shortExpirationJwtService, "secretKey", 
                "mySecretKeyForJWTTokenGenerationThatShouldBeAtLeast256BitsLongForHS256Algorithm");
        ReflectionTestUtils.setField(shortExpirationJwtService, "jwtExpiration", -1L); // Already expired

        String username = "testuser";
        String userType = "TRAINEE";
        String token = shortExpirationJwtService.generateToken(username, userType);

        // The token validation should handle expired tokens gracefully
        assertThrows(RuntimeException.class, () -> {
            shortExpirationJwtService.isTokenValid(token, username);
        });
    }

    @Test
    void testExtractClaims_InvalidToken() {
        String invalidToken = "invalid.jwt.token";

        assertThrows(RuntimeException.class, () -> {
            jwtService.extractUsername(invalidToken);
        });
    }

    @Test
    void testGenerateTokenWithExtraClaims() {
        String username = "testuser";
        java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("department", "IT");

        String token = jwtService.generateToken(extraClaims, username);

        assertNotNull(token);
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    void testTokenContainsRequiredClaims() {
        String username = "testuser";
        String userType = "TRAINEE";
        String token = jwtService.generateToken(username, userType);

        // Extract all claims to verify structure
        String extractedUsername = jwtService.extractUsername(token);
        String extractedUserType = jwtService.extractUserType(token);

        assertEquals(username, extractedUsername);
        assertEquals(userType, extractedUserType);
    }
}

