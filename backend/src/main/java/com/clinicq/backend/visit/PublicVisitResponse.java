package com.clinicq.backend.visit;

public record PublicVisitResponse(
        int tokenNumber,
        Long doctorId,
        String doctorName,
        VisitStatus status,
        Integer position,
        int patientsAhead,
        int estimatedWaitMinutes,
        Integer nowServingToken,
        boolean comeNow
) {}