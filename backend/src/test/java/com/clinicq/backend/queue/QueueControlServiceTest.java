package com.clinicq.backend.queue;

import com.clinicq.backend.config.ConflictException;
import com.clinicq.backend.config.NotFoundException;
import com.clinicq.backend.doctor.Doctor;
import com.clinicq.backend.doctor.DoctorRepository;
import com.clinicq.backend.visit.Visit;
import com.clinicq.backend.visit.VisitRepository;
import com.clinicq.backend.visit.VisitStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueueControlServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private QueueService queueService;

    @Mock
    private QueueBroadcaster broadcaster;

    private QueueControlService service;
    private Doctor doctor;

    private final Long doctorId = 1L;
    private final LocalDate today =
            LocalDate.of(2026, 10, 30);

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-10-30T10:00:00Z"),
                ZoneId.of("UTC")
        );

        service = new QueueControlService(
                doctorRepository,
                visitRepository,
                queueService,
                clock,
                broadcaster
        );

        doctor = mock(Doctor.class);

        when(doctorRepository.findByIdForUpdate(doctorId))
                .thenReturn(Optional.of(doctor));
        when(doctor.isActive()).thenReturn(true);
    }

    @Test
    void nextCallsFirstWaitingPatientWhenNobodyIsBeingServed() {
        QueueSnapshot snapshot = mock(QueueSnapshot.class);

        when(queueService.snapshot(doctorId, today))
                .thenReturn(snapshot);
        when(snapshot.currentToken()).thenReturn(null);

        Visit nextVisit = mock(Visit.class);
        when(doctor.getId()).thenReturn(doctorId);
        when(visitRepository
                .findFirstByDoctorIdAndVisitDateAndStatusOrderByTokenNumberAsc(
                        doctorId, today, VisitStatus.WAITING))
                .thenReturn(Optional.of(nextVisit));
        when(nextVisit.getTokenNumber()).thenReturn(2);

        service.next(doctorId);

        verify(nextVisit).setStatus(VisitStatus.IN_PROGRESS);
        verify(nextVisit).setStartedAt(
                Instant.parse("2026-10-30T10:00:00Z"));
        verify(visitRepository).save(nextVisit);
    }

    @Test
    void doneThrowsConflictWhenNobodyIsBeingServed() {
        QueueSnapshot snapshot = mock(QueueSnapshot.class);

        when(queueService.snapshot(doctorId, today))
                .thenReturn(snapshot);
        when(snapshot.currentToken()).thenReturn(null);

        assertThrows(
                ConflictException.class,
                () -> service.done(doctorId)
        );
    }

    @Test
    void skipDoesNotThrowWhenNobodyIsBeingServed() {
        QueueSnapshot snapshot = mock(QueueSnapshot.class);

        when(queueService.snapshot(doctorId, today))
                .thenReturn(snapshot);
        when(snapshot.currentToken()).thenReturn(null);

        service.skip(doctorId);

        verify(visitRepository, never()).save(any(Visit.class));
    }

    @Test
    void nextThrowsWhenDoctorDoesNotExist() {
        when(doctorRepository.findByIdForUpdate(doctorId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.next(doctorId)
        );
    }
}
