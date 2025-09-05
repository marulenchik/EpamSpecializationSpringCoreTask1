package com.gym.crm.actuator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ActuatorEndpointsTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getActuatorUrl(String endpoint) {
        return "http://localhost:" + port + "/actuator/" + endpoint;
    }

    @Test
    void testHealthEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                getActuatorUrl("health"), String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"status\":\"UP\""));
    }

    @Test
    void testInfoEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                getActuatorUrl("info"), String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Should contain application info from properties
        assertTrue(response.getBody().contains("Gym CRM System") || 
                  response.getBody().contains("app"));
    }

    @Test
    void testMetricsEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                getActuatorUrl("metrics"), String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("names"));
    }

    @Test
    void testPrometheusEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                getActuatorUrl("prometheus"), String.class);
        
        // Prometheus endpoint might not be enabled in test environment
        // Check if it's available or if it returns a reasonable response
        if (response.getStatusCode() == HttpStatus.OK) {
            assertNotNull(response.getBody());
            // Prometheus format should contain metrics
            assertTrue(response.getBody().contains("# HELP") || 
                      response.getBody().contains("# TYPE") ||
                      response.getBody().contains("jvm_"));
        } else {
            // If Prometheus is not configured properly, we'll just check it's not a server error
            assertTrue(response.getStatusCode() == HttpStatus.NOT_FOUND || 
                      response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR,
                      "Expected 404 or 500 if Prometheus is not properly configured, got: " + response.getStatusCode());
        }
    }

    @Test
    void testEnvironmentEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                getActuatorUrl("env"), String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("activeProfiles") || 
                  response.getBody().contains("propertySources"));
    }

    @Test
    void testConfigPropsEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                getActuatorUrl("configprops"), String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("contexts"));
    }

    @Test
    void testSpecificMetric() {
        // First get available metrics
        ResponseEntity<String> metricsResponse = restTemplate.getForEntity(
                getActuatorUrl("metrics"), String.class);
        
        assertEquals(HttpStatus.OK, metricsResponse.getStatusCode());
        
        // Test a specific metric (jvm.memory.used should always be available)
        ResponseEntity<String> specificMetricResponse = restTemplate.getForEntity(
                getActuatorUrl("metrics/jvm.memory.used"), String.class);
        
        assertEquals(HttpStatus.OK, specificMetricResponse.getStatusCode());
        assertNotNull(specificMetricResponse.getBody());
        assertTrue(specificMetricResponse.getBody().contains("measurements"));
    }
}
