package com.fabianlicea.jobtrack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @Email @NotBlank(message = "Email is required") String email,

        @NotBlank(message = "Password is required") @Size(min = 8, message = "Password cannot be less than 8 characters") String password

) {

}
