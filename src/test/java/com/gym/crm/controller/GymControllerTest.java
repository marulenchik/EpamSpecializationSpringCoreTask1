package com.gym.crm.controller;

import com.gym.crm.dto.request.*;
import com.gym.crm.facade.GymCrmFacade;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GymController.class)
class GymControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private GymCrmFacade gymCrmFacade;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private Trainee testTrainee;
    private Trainer testTrainer;
    private TrainingType testTrainingType;
    
    @BeforeEach
    void setUp() {
        testTrainingType = new TrainingType(1L, "Fitness");
        
        testTrainer = new Trainer("Jane", "Smith", testTrainingType);
        testTrainer.setId(1L);
        testTrainer.setUsername("jane.smith");
        testTrainer.setPassword("password123");
        testTrainer.setIsActive(true);
        
        testTrainee = new Trainee("John", "Doe", LocalDate.of(1990, 1, 1), "123 Main St");
        testTrainee.setId(1L);
        testTrainee.setUsername("john.doe");
        testTrainee.setPassword("password123");
        testTrainee.setIsActive(true);
    }
    
    @Test
    void testLogin_TraineeSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("john.doe");
        request.setPassword("password123");
        
        when(gymCrmFacade.matchTraineeCredentials("john.doe", "password123")).thenReturn(true);
        when(gymCrmFacade.matchTrainerCredentials("john.doe", "password123")).thenReturn(false);
        
        mockMvc.perform(get("/api/login")
                .param("username", "john.doe")
                .param("password", "password123"))
                .andExpect(status().isOk());
        
        verify(gymCrmFacade).matchTraineeCredentials("john.doe", "password123");
        verify(gymCrmFacade).matchTrainerCredentials("john.doe", "password123");
    }
    
    @Test
    void testLogin_TrainerSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("jane.smith");
        request.setPassword("password123");
        
        when(gymCrmFacade.matchTraineeCredentials("jane.smith", "password123")).thenReturn(false);
        when(gymCrmFacade.matchTrainerCredentials("jane.smith", "password123")).thenReturn(true);
        
        mockMvc.perform(get("/api/login")
                .param("username", "jane.smith")
                .param("password", "password123"))
                .andExpect(status().isOk());
        
        verify(gymCrmFacade).matchTraineeCredentials("jane.smith", "password123");
        verify(gymCrmFacade).matchTrainerCredentials("jane.smith", "password123");
    }
    
    @Test
    void testLogin_Failed() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("john.doe");
        request.setPassword("wrongpassword");
        
        when(gymCrmFacade.matchTraineeCredentials("john.doe", "wrongpassword")).thenReturn(false);
        when(gymCrmFacade.matchTrainerCredentials("john.doe", "wrongpassword")).thenReturn(false);
        
        mockMvc.perform(get("/api/login")
                .param("username", "john.doe")
                .param("password", "wrongpassword"))
                .andExpect(status().isUnauthorized());
        
        verify(gymCrmFacade).matchTraineeCredentials("john.doe", "wrongpassword");
        verify(gymCrmFacade).matchTrainerCredentials("john.doe", "wrongpassword");
    }
    
    @Test
    void testChangePassword_TraineeSuccess() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("john.doe");
        request.setOldPassword("oldpassword");
        request.setNewPassword("newpassword");
        
        when(gymCrmFacade.changeTraineePassword("john.doe", "oldpassword", "newpassword")).thenReturn(true);
        
        mockMvc.perform(put("/api/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        verify(gymCrmFacade).changeTraineePassword("john.doe", "oldpassword", "newpassword");
        verify(gymCrmFacade, never()).changeTrainerPassword(any(), any(), any());
    }
    
    @Test
    void testChangePassword_TrainerSuccess() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("jane.smith");
        request.setOldPassword("oldpassword");
        request.setNewPassword("newpassword");
        
        when(gymCrmFacade.changeTraineePassword("jane.smith", "oldpassword", "newpassword")).thenReturn(false);
        when(gymCrmFacade.changeTrainerPassword("jane.smith", "oldpassword", "newpassword")).thenReturn(true);
        
        mockMvc.perform(put("/api/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        verify(gymCrmFacade).changeTraineePassword("jane.smith", "oldpassword", "newpassword");
        verify(gymCrmFacade).changeTrainerPassword("jane.smith", "oldpassword", "newpassword");
    }
    
    @Test
    void testChangePassword_Failed() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("john.doe");
        request.setOldPassword("wrongpassword");
        request.setNewPassword("newpassword");
        
        when(gymCrmFacade.changeTraineePassword("john.doe", "wrongpassword", "newpassword")).thenReturn(false);
        when(gymCrmFacade.changeTrainerPassword("john.doe", "wrongpassword", "newpassword")).thenReturn(false);
        
        mockMvc.perform(put("/api/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        
        verify(gymCrmFacade).changeTraineePassword("john.doe", "wrongpassword", "newpassword");
        verify(gymCrmFacade).changeTrainerPassword("john.doe", "wrongpassword", "newpassword");
    }
    
    @Test
    void testAddTraining_Success() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("john.doe");
        request.setTrainerUsername("jane.smith");
        request.setTrainingName("Morning Workout");
        request.setTrainingDate(LocalDate.now());
        request.setTrainingDuration(60);
        
        when(gymCrmFacade.getTraineeByUsername("john.doe")).thenReturn(Optional.of(testTrainee));
        when(gymCrmFacade.getTrainerByUsername("jane.smith")).thenReturn(Optional.of(testTrainer));
        
        mockMvc.perform(post("/api/trainings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        verify(gymCrmFacade).getTraineeByUsername("john.doe");
        verify(gymCrmFacade).getTrainerByUsername("jane.smith");
        verify(gymCrmFacade).addTraining(any());
    }
    
    @Test
    void testAddTraining_TraineeNotFound() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("nonexistent");
        request.setTrainerUsername("jane.smith");
        request.setTrainingName("Morning Workout");
        request.setTrainingDate(LocalDate.now());
        request.setTrainingDuration(60);
        
        when(gymCrmFacade.getTraineeByUsername("nonexistent")).thenReturn(Optional.empty());
        
        mockMvc.perform(post("/api/trainings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        verify(gymCrmFacade).getTraineeByUsername("nonexistent");
        verify(gymCrmFacade, never()).addTraining(any());
    }
    
    @Test
    void testGetTrainingTypes_Success() throws Exception {
        TrainingType type1 = new TrainingType(1L, "Fitness");
        TrainingType type2 = new TrainingType(2L, "Yoga");
        
        when(gymCrmFacade.getAllTrainingTypes()).thenReturn(Arrays.asList(type1, type2));
        
        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].trainingTypeName").value("Fitness"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].trainingTypeName").value("Yoga"));
        
        verify(gymCrmFacade).getAllTrainingTypes();
    }
    
    @Test
    void testAddTraining_ValidationError() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername(""); // Invalid - empty
        request.setTrainerUsername("jane.smith");
        request.setTrainingName("Morning Workout");
        request.setTrainingDate(LocalDate.now());
        request.setTrainingDuration(60);
        
        mockMvc.perform(post("/api/trainings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
        
        verify(gymCrmFacade, never()).addTraining(any());
    }
}
