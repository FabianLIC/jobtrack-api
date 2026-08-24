package com.fabianlicea.jobtrack.dto;

import java.time.LocalDate;

import org.hibernate.validator.constraints.URL;

import com.fabianlicea.jobtrack.model.ApplicationStatus;
import com.fabianlicea.jobtrack.model.WorkMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ApplicationRequest(

                @NotBlank(message = "Company name is required") @Size(max = 100, message = "Company name cannot exceed 100 characters") String company,

                @NotBlank(message = "Position is required") @Size(max = 100, message = "Position cannot exceed 100 characters") String position,

                ApplicationStatus status,

                @Size(max = 100, message = "Location cannot exceed 100 characters") String location,

                WorkMode workMode,

                @Positive(message = "The salary must be positive number") Integer salaryMin,

                @Positive(message = "The salary must be positive number") Integer salaryMax,

                @URL(message = "Offer URL is not valid") String offerUrl,

                @Size(max = 100, message = "Source cannot exceed 100 characters") String source,

                @PastOrPresent(message = "Applied date cannot be in the future") LocalDate appliedAt) {

}
