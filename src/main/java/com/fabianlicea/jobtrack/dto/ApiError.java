package com.fabianlicea.jobtrack.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError (LocalDateTime errorAt, int statusCode, String error, String message, String failedPath, Map<String, String> failedErrors) {
    public ApiError {
        if (errorAt == null) {
            errorAt = LocalDateTime.now();
        }
    }

}
