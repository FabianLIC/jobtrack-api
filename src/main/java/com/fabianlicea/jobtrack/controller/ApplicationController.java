package com.fabianlicea.jobtrack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fabianlicea.jobtrack.dto.ApplicationResponse;
import com.fabianlicea.jobtrack.service.ApplicationService;
import java.util.List;

// Le dice a Spring que esta clase gestiona peticiones HTTP y que las respuestas se devuelven como JSON automáticamente (en lugar de HTML)
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public List<ApplicationResponse> listAll() {
        return applicationService.findAll();
    }

}