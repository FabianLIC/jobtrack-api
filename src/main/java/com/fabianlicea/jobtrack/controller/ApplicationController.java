package com.fabianlicea.jobtrack.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fabianlicea.jobtrack.dto.ApplicationRequest;
import com.fabianlicea.jobtrack.dto.ApplicationResponse;
import com.fabianlicea.jobtrack.security.UserPrincipal;
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

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Operation(summary = "Get all applications")
    @GetMapping
    public List<ApplicationResponse> listAll(@AuthenticationPrincipal UserPrincipal principal) {
        return applicationService.findAll(principal.getId());
    }

    @Operation(summary = "Get an application by id")
    @GetMapping("/{id}")
    public ApplicationResponse findApplicationById(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return applicationService.findById(id, principal.getId());
    }

    @Operation(summary = "Create an application")
    @PostMapping()
    public ResponseEntity<ApplicationResponse> createApplication(@Valid @RequestBody ApplicationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.create(request, principal.getId()));
    }

    @Operation(summary = "Update an application by id")
    @PutMapping("/{id}")
    public ApplicationResponse updateApplication(@PathVariable Long id, @Valid @RequestBody ApplicationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return applicationService.update(id, principal.getId(), request);
    }

    @Operation(summary = "Delete an application by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        applicationService.delete(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

}