package com.fabianlicea.jobtrack.exceptions;

public class ApplicationNotFoundException extends RuntimeException {
    
    public ApplicationNotFoundException(Long id) {
        super("Application not found with id: " + id);
    }
}
