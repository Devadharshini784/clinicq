package com.clinicq.backend.visit;

import java.time.LocalDate;

public record VisitResponse(
        Long id,
        String publicId,
        int tokenNumber,
        String patientName,
        String doctorName,
        VisitStatus status,
        LocalDate visitDate,
        String trackingPath
) {
    public static VisitResponse from(Visit v) {
        return new VisitResponse(
                v.getId(),
                v.getPublicId(),
                v.getTokenNumber(),
                v.getPatient().getName(),
                v.getDoctor().getName(),
                v.getStatus(),
                v.getVisitDate(),
                "/q/" + v.getPublicId()
        );
    }
}