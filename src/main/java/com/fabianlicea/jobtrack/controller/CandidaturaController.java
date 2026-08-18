package com.fabianlicea.jobtrack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

// Le dice a Spring que esta clase gestiona peticiones HTTP y que las respuestas se devuelven como JSON automáticamente (en lugar de HTML)
@RestController
@RequestMapping("/api/candidaturas")
public class CandidaturaController {

    @GetMapping
    public List<Object> listar() {
        return new ArrayList<>();
    }
}