package com.clinicq.backend.queue;

import com.clinicq.backend.config.ConflictException;
import com.clinicq.backend.config.NotFoundException;
import com.clinicq.backend.doctor.Doctor;
import com.clinicq.backend.doctor.DoctorRepository;
import com.clinicq.backend.visit.Visit;
import com.clinicq.backend.visit.VisitRepository;
import com.clinicq.backend.visit.VisitStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

@Service
public class QueueControlService {

    private final DoctorRepository doctorRepository;
    private final VisitRepository visitRepository;
    private final QueueService queueService;
    private final Clock clock;

    public QueueControlService(
            DoctorRepository doctorRepository,
            VisitRepository visitRepository,
            QueueService queueService,
            Clock clock) {
        this.doctorRepository = doctorRepository;
        this.visitRepository = visitRepository;
        this.queueService = queueService;
        this.clock = clock;
    }

    @Transactional
    public void next(Long doctorId) {
        Doctor doctor = lockDoctor(doctorId);
        LocalDate today = LocalDate.now(clock);

        QueueSnapshot snapshot = queueService.snapshot(doctorId, today);

        if (snapshot.currentToken() != null) {
            finishCurrent(doctorId, today, VisitStatus.DONE);
        }

        callNext(doctor, today);
    }

    @Transactional
    public void skip(Long doctorId) {
        Doctor doctor = lockDoctor(doctorId);
        LocalDate today = LocalDate.now(clock);

        finishCurrent(doctorId, today, VisitStatus.SKIPPED);
        callNext(doctor, today);
    }

    @Transactional
    public void done(Long doctorId) {
        lockDoctor(doctorId);
        LocalDate today = LocalDate.now(clock);

        finishCurrent(doctorId, today, VisitStatus.DONE);
    }

    private Doctor lockDoctor(Long doctorId) {
        return doctorRepository.findByIdForUpdate(doctorId)
                .filter(Doctor::isActive)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));
    }

    private void finishCurrent(
            Long doctorId,
            LocalDate today,
            VisitStatus finalStatus) {

        QueueSnapshot snapshot = queueService.snapshot(doctorId, today);
        Integer currentToken = snapshot.currentToken();

        if (currentToken == null) {
            if (finalStatus == VisitStatus.DONE) {
                throw new ConflictException("No patient is currently being served");
            }
            return;
        }

        Visit currentVisit = visitRepository
                .findByDoctorIdAndVisitDateAndTokenNumber(doctorId, today, currentToken)
                .orElseThrow(() -> new NotFoundException("Current visit not found"));

        Instant finishedAt = Instant.now(clock);
        currentVisit.setStatus(finalStatus);
        currentVisit.setFinishedAt(finishedAt);
        visitRepository.save(currentVisit);

        if (finalStatus == VisitStatus.DONE && currentVisit.getStartedAt() != null) {
            Instant startedAt = currentVisit.getStartedAt();
            com.clinicq.backend.config.AfterCommit.run(
                    () -> queueService.recordDuration(doctorId, startedAt, finishedAt));
        }

        com.clinicq.backend.config.AfterCommit.run(
                () -> queueService.clearCurrent(doctorId, today));
    }

    private void callNext(Doctor doctor, LocalDate today) {
        Long doctorId = doctor.getId();

        Visit nextVisit = visitRepository
                .findFirstByDoctorIdAndVisitDateAndStatusOrderByTokenNumberAsc(
                        doctorId, today, VisitStatus.WAITING)
                .orElse(null);

        if (nextVisit == null) {
            return;
        }

        Instant startedAt = Instant.now(clock);
        nextVisit.setStatus(VisitStatus.IN_PROGRESS);
        nextVisit.setStartedAt(startedAt);
        visitRepository.save(nextVisit);

        int token = nextVisit.getTokenNumber();

        com.clinicq.backend.config.AfterCommit.run(
                () -> queueService.setCurrent(doctorId, today, token));
    }
}