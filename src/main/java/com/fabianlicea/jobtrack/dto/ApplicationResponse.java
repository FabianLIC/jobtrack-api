package com.fabianlicea.jobtrack.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fabianlicea.jobtrack.model.ApplicationStatus;
import com.fabianlicea.jobtrack.model.WorkMode;

public record ApplicationResponse(Long id, String company, String position, ApplicationStatus status, String location,
        WorkMode workMode, Integer salaryMin, Integer salaryMax, String offerUrl, String source, LocalDate appliedAt,
        LocalDateTime createdAt, LocalDateTime updatedAt) {
}