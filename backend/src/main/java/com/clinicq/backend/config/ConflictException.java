package com.clinicq.backend.config;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}