package com.clinicq.backend.config;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}