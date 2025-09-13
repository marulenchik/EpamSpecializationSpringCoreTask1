package com.gym.crm.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.dto.request.LoginRequest;
import com.gym.crm.dto.request.TraineeRegistrationRequest;
import com.gym.crm.dto.response.LoginResponse;
import com.gym.crm.dto.response.RegistrationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testPublicEndpointsAccessible() throws Exception {
        // Test trainee registration (should be accessible without authentication)
        TraineeRegistrationRequest registrationRequest = new TraineeRegistrationRequest();
        registrationRequest.setFirstName("John");
        registrationRequest.setLastName("Doe");

        mockMvc.perform(post("/api/trainees/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testLoginEndpoint() throws Exception {
        // First register a user
        TraineeRegistrationRequest registrationRequest = new TraineeRegistrationRequest();
        registrationRequest.setFirstName("Jane");
        registrationRequest.setLastName("Smith");

        MvcResult registrationResult = mockMvc.perform(post("/api/trainees/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk())
                .andReturn();

        RegistrationResponse registrationResponse = objectMapper.readValue(
                registrationResult.getResponse().getContentAsString(), 
                RegistrationResponse.class);

        // Test login with valid credentials
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(registrationResponse.getUsername());
        loginRequest.setPassword(registrationResponse.getPassword());

        MvcResult loginResult = mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), 
                LoginResponse.class);

        assertNotNull(loginResponse.getToken());
        assertEquals("Bearer", loginResponse.getTokenType());
        assertEquals(registrationResponse.getUsername(), loginResponse.getUsername());
        assertEquals("TRAINEE", loginResponse.getUserType());
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testProtectedEndpointWithoutToken() throws Exception {
        // Test accessing a protected endpoint without JWT token
        mockMvc.perform(get("/api/trainees/john.doe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testProtectedEndpointWithValidToken() throws Exception {
        // First register and login to get a token
        TraineeRegistrationRequest registrationRequest = new TraineeRegistrationRequest();
        registrationRequest.setFirstName("Alice");
        registrationRequest.setLastName("Johnson");

        MvcResult registrationResult = mockMvc.perform(post("/api/trainees/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk())
                .andReturn();

        RegistrationResponse registrationResponse = objectMapper.readValue(
                registrationResult.getResponse().getContentAsString(), 
                RegistrationResponse.class);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(registrationResponse.getUsername());
        loginRequest.setPassword(registrationResponse.getPassword());

        MvcResult loginResult = mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), 
                LoginResponse.class);

        // Test accessing protected endpoint with valid JWT token
        mockMvc.perform(get("/api/trainees/" + registrationResponse.getUsername())
                .header("Authorization", "Bearer " + loginResponse.getToken()))
                .andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpointWithInvalidToken() throws Exception {
        // Test accessing protected endpoint with invalid JWT token
        mockMvc.perform(get("/api/trainees/john.doe")
                .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogoutEndpoint() throws Exception {
        mockMvc.perform(post("/api/logout"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"Logout successful\"}"));
    }

    @Test
    void testCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/login")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }
}
