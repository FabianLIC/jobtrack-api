package com.fabianlicea.jobtrack.dto;

public record AuthResponse(String token, String type, String email, String name) {
}
