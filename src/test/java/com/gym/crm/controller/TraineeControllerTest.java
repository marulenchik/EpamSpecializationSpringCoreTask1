package com.gym.crm.controller;

import com.gym.crm.dto.request.*;
import com.gym.crm.facade.GymCrmFacade;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
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
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TraineeController.class)
class TraineeControllerTest {
    
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
        testTrainee.setTrainers(new HashSet<>(Arrays.asList(testTrainer)));
    }
    
    @Test
    void testRegisterTrainee_Success() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setAddress("123 Main St");
        
        when(gymCrmFacade.createTrainee(any(Trainee.class))).thenReturn(testTrainee);
        
        mockMvc.perform(post("/api/trainees/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.password").value("password123"));
        
        verify(gymCrmFacade).createTrainee(any(Trainee.class));
    }
    
    @Test
    void testRegisterTrainee_ValidationError() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName(""); // Invalid - empty
        request.setLastName("Doe");
        
        mockMvc.perform(post("/api/trainees/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
        
        verify(gymCrmFacade, never()).createTrainee(any());
    }
    
    @Test
    void testGetTraineeProfile_Success() throws Exception {
        when(gymCrmFacade.matchTraineeCredentials("john.doe", "password123")).thenReturn(true);
        when(gymCrmFacade.getTraineeByUsername("john.doe")).thenReturn(Optional.of(testTrainee));
        
        mockMvc.perform(get("/api/trainees/john.doe")
                .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.trainers").isArray())
                .andExpect(jsonPath("$.trainers[0].username").value("jane.smith"));
        
        verify(gymCrmFacade).matchTraineeCredentials("john.doe", "password123");
        verify(gymCrmFacade).getTraineeByUsername("john.doe");
    }
    
    @Test
    void testGetTraineeProfile_AuthenticationFailed() throws Exception {
        when(gymCrmFacade.matchTraineeCredentials("john.doe", "wrongpassword")).thenReturn(false);
        
        mockMvc.perform(get("/api/trainees/john.doe")
                .param("password", "wrongpassword"))
                .andExpect(status().isUnauthorized());
        
        verify(gymCrmFacade).matchTraineeCredentials("john.doe", "wrongpassword");
        verify(gymCrmFacade, never()).getTraineeByUsername(any());
    }
    
    @Test
    void testUpdateTraineeProfile_Success() throws Exception {
        TraineeUpdateRequest request = new TraineeUpdateRequest();
        request.setUsername("john.doe");
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setAddress("456 Oak St");
        request.setIsActive(true);
        
        Trainee updatedTrainee = new Trainee("John", "Smith", LocalDate.of(1990, 1, 1), "456 Oak St");
        updatedTrainee.setUsername("john.doe");
        updatedTrainee.setIsActive(true);
        updatedTrainee.setTrainers(new HashSet<>(Arrays.asList(testTrainer)));
        
        when(gymCrmFacade.updateTrainee(eq("john.doe"), eq("password123"), any(Trainee.class)))
                .thenReturn(updatedTrainee);
        
        mockMvc.perform(put("/api/trainees")
                .param("password", "password123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.address").value("456 Oak St"));
        
        verify(gymCrmFacade).updateTrainee(eq("john.doe"), eq("password123"), any(Trainee.class));
    }
    
    @Test
    void testDeleteTraineeProfile_Success() throws Exception {
        when(gymCrmFacade.deleteTraineeByUsername("john.doe", "password123")).thenReturn(true);
        
        mockMvc.perform(delete("/api/trainees/john.doe")
                .param("password", "password123"))
                .andExpect(status().isOk());
        
        verify(gymCrmFacade).deleteTraineeByUsername("john.doe", "password123");
    }
    
    @Test
    void testGetTraineeTrainings_Success() throws Exception {
        Training training = new Training(testTrainee, testTrainer, "Morning Workout", 
                                       testTrainingType, LocalDate.now(), 60);
        
        when(gymCrmFacade.getTraineeTrainingsList(eq("john.doe"), eq("password123"), 
                isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Arrays.asList(training));
        
        mockMvc.perform(get("/api/trainees/john.doe/trainings")
                .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].trainingName").value("Morning Workout"))
                .andExpect(jsonPath("$[0].trainingType").value("Fitness"))
                .andExpect(jsonPath("$[0].trainingDuration").value(60));
        
        verify(gymCrmFacade).getTraineeTrainingsList(eq("john.doe"), eq("password123"), 
                isNull(), isNull(), isNull(), isNull());
    }
    
    @Test
    void testUpdateTraineeTrainersList_Success() throws Exception {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();
        request.setTraineeUsername("john.doe");
        request.setTrainerUsernames(Arrays.asList("jane.smith"));
        
        when(gymCrmFacade.updateTraineeTrainersList("john.doe", "password123", 
                Arrays.asList("jane.smith"))).thenReturn(testTrainee);
        
        mockMvc.perform(put("/api/trainees/trainers")
                .param("password", "password123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("jane.smith"));
        
        verify(gymCrmFacade).updateTraineeTrainersList("john.doe", "password123", 
                Arrays.asList("jane.smith"));
    }
    
    @Test
    void testActivateDeactivateTrainee_Success() throws Exception {
        ActivationRequest request = new ActivationRequest();
        request.setUsername("john.doe");
        request.setIsActive(false);
        
        when(gymCrmFacade.deactivateTrainee("john.doe", "password123")).thenReturn(true);
        
        mockMvc.perform(patch("/api/trainees/activate")
                .param("password", "password123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        verify(gymCrmFacade).deactivateTrainee("john.doe", "password123");
    }
    
    @Test
    void testActivateDeactivateTrainee_AlreadyInDesiredState() throws Exception {
        ActivationRequest request = new ActivationRequest();
        request.setUsername("john.doe");
        request.setIsActive(false);
        
        when(gymCrmFacade.deactivateTrainee("john.doe", "password123")).thenReturn(false);
        
        mockMvc.perform(patch("/api/trainees/activate")
                .param("password", "password123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        verify(gymCrmFacade).deactivateTrainee("john.doe", "password123");
    }
}
