package com.clinicq.backend.queue;

import com.clinicq.backend.visit.VisitResponse;

import java.util.List;

public record DoctorQueueResponse(
        QueueSnapshot queue,
        List<VisitResponse> todaysVisits
) {
}