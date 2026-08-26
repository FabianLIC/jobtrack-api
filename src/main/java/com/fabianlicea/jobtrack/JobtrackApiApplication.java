package com.fabianlicea.jobtrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(info = @Info(title = "JobTrack API", version = "1.0", description = "REST API for tracking job applications"))

@SpringBootApplication
public class JobtrackApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobtrackApiApplication.class, args);
    }

}