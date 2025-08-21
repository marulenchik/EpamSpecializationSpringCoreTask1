package com.gym.crm.integration;

import com.gym.crm.dto.request.*;
import com.gym.crm.dto.response.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebMvc
@TestPropertySource(properties = {
        "logging.level.com.gym.crm=ERROR",
        "spring.jpa.show-sql=false"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class RestApiIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testCompleteRestApiWorkflow() throws Exception {
        // 1. Get training types first
        String trainingTypesResponse = mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].trainingTypeName").exists())
                .andReturn().getResponse().getContentAsString();
        
        TrainingTypeDto[] trainingTypes = objectMapper.readValue(trainingTypesResponse, TrainingTypeDto[].class);
        Long fitnessTypeId = Arrays.stream(trainingTypes)
                .filter(type -> "Fitness".equals(type.getTrainingTypeName()))
                .findFirst()
                .map(TrainingTypeDto::getId)
                .orElse(1L);
        
        // 2. Register trainer
        TrainerRegistrationRequest trainerRequest = new TrainerRegistrationRequest();
        trainerRequest.setFirstName("Jane");
        trainerRequest.setLastName("Smith");
        trainerRequest.setSpecializationId(fitnessTypeId);
        
        String trainerResponse = mockMvc.perform(post("/api/trainers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trainerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.password").exists())
                .andReturn().getResponse().getContentAsString();
        
        RegistrationResponse trainerRegistration = objectMapper.readValue(trainerResponse, RegistrationResponse.class);
        
        // 3. Register trainee
        TraineeRegistrationRequest traineeRequest = new TraineeRegistrationRequest();
        traineeRequest.setFirstName("John");
        traineeRequest.setLastName("Doe");
        traineeRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        traineeRequest.setAddress("123 Main St");
        
        String traineeResponse = mockMvc.perform(post("/api/trainees/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(traineeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.password").exists())
                .andReturn().getResponse().getContentAsString();
        
        RegistrationResponse traineeRegistration = objectMapper.readValue(traineeResponse, RegistrationResponse.class);
        
        // 4. Test login for both users
        mockMvc.perform(get("/api/login")
                .param("username", trainerRegistration.getUsername())
                .param("password", trainerRegistration.getPassword()))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/login")
                .param("username", traineeRegistration.getUsername())
                .param("password", traineeRegistration.getPassword()))
                .andExpect(status().isOk());
        
        // 5. Get trainer profile
        mockMvc.perform(get("/api/trainers/" + trainerRegistration.getUsername())
                .param("password", trainerRegistration.getPassword()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.specialization").value("Fitness"))
                .andExpect(jsonPath("$.isActive").value(true));
        
        // 6. Get trainee profile
        mockMvc.perform(get("/api/trainees/" + traineeRegistration.getUsername())
                .param("password", traineeRegistration.getPassword()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.trainers").isArray());
        
        // 7. Update trainee's trainers list
        UpdateTraineeTrainersRequest updateTrainersRequest = new UpdateTraineeTrainersRequest();
        updateTrainersRequest.setTraineeUsername(traineeRegistration.getUsername());
        updateTrainersRequest.setTrainerUsernames(Arrays.asList(trainerRegistration.getUsername()));
        
        mockMvc.perform(put("/api/trainees/trainers")
                .param("password", traineeRegistration.getPassword())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTrainersRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value(trainerRegistration.getUsername()));
        
        // 8. Add training
        AddTrainingRequest trainingRequest = new AddTrainingRequest();
        trainingRequest.setTraineeUsername(traineeRegistration.getUsername());
        trainingRequest.setTrainerUsername(trainerRegistration.getUsername());
        trainingRequest.setTrainingName("Morning Workout");
        trainingRequest.setTrainingDate(LocalDate.now());
        trainingRequest.setTrainingDuration(60);
        
        mockMvc.perform(post("/api/trainings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trainingRequest)))
                .andExpect(status().isOk());
        
        // 9. Get trainee trainings
        mockMvc.perform(get("/api/trainees/" + traineeRegistration.getUsername() + "/trainings")
                .param("password", traineeRegistration.getPassword()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].trainingName").value("Morning Workout"));
        
        // 10. Get trainer trainings
        mockMvc.perform(get("/api/trainers/" + trainerRegistration.getUsername() + "/trainings")
                .param("password", trainerRegistration.getPassword()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].trainingName").value("Morning Workout"));
        
        // 11. Change password
        ChangePasswordRequest passwordRequest = new ChangePasswordRequest();
        passwordRequest.setUsername(traineeRegistration.getUsername());
        passwordRequest.setOldPassword(traineeRegistration.getPassword());
        passwordRequest.setNewPassword("newPassword123");
        
        mockMvc.perform(put("/api/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordRequest)))
                .andExpect(status().isOk());
        
        // 12. Verify new password works
        mockMvc.perform(get("/api/login")
                .param("username", traineeRegistration.getUsername())
                .param("password", "newPassword123"))
                .andExpect(status().isOk());
        
        // 13. Update trainee profile
        TraineeUpdateRequest updateRequest = new TraineeUpdateRequest();
        updateRequest.setUsername(traineeRegistration.getUsername());
        updateRequest.setFirstName("John");
        updateRequest.setLastName("Smith");
        updateRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        updateRequest.setAddress("456 Oak St");
        updateRequest.setIsActive(true);
        
        mockMvc.perform(put("/api/trainees")
                .param("password", "newPassword123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.address").value("456 Oak St"));
        
        // 14. Deactivate trainee
        ActivationRequest deactivateRequest = new ActivationRequest();
        deactivateRequest.setUsername(traineeRegistration.getUsername());
        deactivateRequest.setIsActive(false);
        
        mockMvc.perform(patch("/api/trainees/activate")
                .param("password", "newPassword123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deactivateRequest)))
                .andExpect(status().isOk());
        
        // 15. Verify trainee is deactivated
        mockMvc.perform(get("/api/trainees/" + traineeRegistration.getUsername())
                .param("password", "newPassword123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
        
        // 16. Get unassigned trainers (should work even though trainee is deactivated)
        mockMvc.perform(get("/api/trainers/unassigned")
                .param("traineeUsername", traineeRegistration.getUsername())
                .param("password", "newPassword123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    void testAuthenticationFailures() throws Exception {
        // Test login with invalid credentials
        mockMvc.perform(get("/api/login")
                .param("username", "nonexistent")
                .param("password", "wrongpassword"))
                .andExpect(status().isUnauthorized());
        
        // Test accessing protected endpoint without authentication
        mockMvc.perform(get("/api/trainees/nonexistent")
                .param("password", "wrongpassword"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testValidationErrors() throws Exception {
        // Test registration with invalid data
        TraineeRegistrationRequest invalidRequest = new TraineeRegistrationRequest();
        invalidRequest.setFirstName(""); // Invalid - empty
        invalidRequest.setLastName("Doe");
        
        mockMvc.perform(post("/api/trainees/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
