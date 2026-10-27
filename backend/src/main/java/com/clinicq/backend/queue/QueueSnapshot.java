package com.clinicq.backend.queue;

import java.time.LocalDate;
import java.util.List;

public record QueueSnapshot(
        Long doctorId,
        LocalDate date,
        Integer currentToken,
        List<Integer> waitingTokens,
        int avgConsultationSeconds
) {
    public int patientsAhead(int token) {
        int index = waitingTokens.indexOf(token);

        if (index < 0) {
            return 0;
        }

        return index + (currentToken != null ? 1 : 0);
    }

    public int estimatedWaitSeconds(int token) {
        return patientsAhead(token) * avgConsultationSeconds;
    }
}