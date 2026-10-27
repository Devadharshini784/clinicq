package com.clinicq.backend.visit;

public record PublicVisitResponse(
        int tokenNumber,
        String doctorName,
        VisitStatus status,
        long patientsAhead
) {}