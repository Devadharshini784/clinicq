package com.clinicq.backend.queue;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueueSnapshotTest {

    private QueueSnapshot snapshot(Integer current, List<Integer> waiting) {
        return new QueueSnapshot(
                1L,
                LocalDate.of(2026, 11, 2),
                current,
                waiting,
                300
        );
    }

    @Test
    void firstInLineWithNobodyInsideHasNobodyAhead() {
        QueueSnapshot s = snapshot(null, List.of(1, 2, 3));

        assertEquals(0, s.patientsAhead(1));
        assertEquals(0, s.estimatedWaitSeconds(1));
    }

    @Test
    void patientWithTheDoctorCountsAsAhead() {
        QueueSnapshot s = snapshot(4, List.of(5, 6, 7));

        assertEquals(1, s.patientsAhead(5));
        assertEquals(3, s.patientsAhead(7));
    }

    @Test
    void waitIsPatientsAheadTimesAverageConsultation() {
        QueueSnapshot s = snapshot(4, List.of(5, 6, 7));

        assertEquals(900, s.estimatedWaitSeconds(7));
    }

    @Test
    void tokenThatIsNotWaitingHasNobodyAhead() {
        QueueSnapshot s = snapshot(4, List.of(5, 6));

        assertEquals(0, s.patientsAhead(99));
        assertEquals(0, s.estimatedWaitSeconds(99));
    }
}