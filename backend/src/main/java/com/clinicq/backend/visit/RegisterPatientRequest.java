package com.clinicq.backend.visit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegisterPatientRequest(
        @NotBlank(message = "Patient name is required")
        String name,

        @Pattern(
                regexp = "^$|^[0-9+ -]{7,15}$",
                message = "Invalid phone number"
        )
        String phone,

        @NotNull(message = "Doctor is required")
        Long doctorId
) {}