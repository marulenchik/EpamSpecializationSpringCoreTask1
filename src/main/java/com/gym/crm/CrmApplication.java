package com.gym.crm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@PropertySource("classpath:application.properties")
@EnableDiscoveryClient
@Slf4j
public class CrmApplication {

	public static void main(String[] args) {
		log.info("Starting Gym CRM Application...");
		SpringApplication.run(CrmApplication.class, args);
		log.info("Gym CRM Application started successfully!");
	}
}
