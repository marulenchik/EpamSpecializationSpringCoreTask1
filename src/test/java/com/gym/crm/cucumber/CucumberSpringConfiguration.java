package com.gym.crm.cucumber;

import com.gym.crm.CrmApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(classes = CrmApplication.class)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {
}

