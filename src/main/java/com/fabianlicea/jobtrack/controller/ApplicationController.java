package com.fabianlicea.jobtrack.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fabianlicea.jobtrack.dto.ApplicationRequest;
import com.fabianlicea.jobtrack.dto.ApplicationResponse;
import com.fabianlicea.jobtrack.service.ApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@Tag(name = "Applications", description = "Manage job applications")
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    // TODO: replace with authenticated user id when Spring Security is implemented
    private static final Long TEMP_USER_ID = 1L;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Operation(summary = "Get all applications")
    @GetMapping
    public List<ApplicationResponse> listAll() {
        return applicationService.findAll(TEMP_USER_ID);
    }

    @Operation(summary = "Get an application by id")
    @GetMapping("/{id}")
    public ApplicationResponse findApplicationById(@PathVariable Long id) {
        return applicationService.findById(id, TEMP_USER_ID);
    }

    @Operation(summary = "Create an application")
    @PostMapping()
    public ResponseEntity<ApplicationResponse> createApplication(@Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.create(request, TEMP_USER_ID));
    }

    @Operation(summary = "Update an application by id")
    @PutMapping("/{id}")
    public ApplicationResponse updateApplication(@PathVariable Long id, @Valid @RequestBody ApplicationRequest request) {
        return applicationService.update(id, TEMP_USER_ID, request);
    }

    @Operation(summary = "Delete an application by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.delete(id, TEMP_USER_ID);
        return ResponseEntity.noContent().build();
    }

}