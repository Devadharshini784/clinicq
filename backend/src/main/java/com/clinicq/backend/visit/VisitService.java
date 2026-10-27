package com.clinicq.backend.visit;

import com.clinicq.backend.config.AfterCommit;
import com.clinicq.backend.config.NotFoundException;
import com.clinicq.backend.doctor.Doctor;
import com.clinicq.backend.doctor.DoctorRepository;
import com.clinicq.backend.patient.Patient;
import com.clinicq.backend.patient.PatientRepository;
import com.clinicq.backend.queue.QueueService;
import com.clinicq.backend.queue.QueueSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class VisitService {

    private static final int COME_NOW_THRESHOLD = 2;

    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final QueueService queueService;
    private final Clock clock;

    public VisitService(
            VisitRepository visitRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            QueueService queueService,
            Clock clock) {
        this.visitRepository = visitRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.queueService = queueService;
        this.clock = clock;
    }

    @Transactional
    public VisitResponse register(RegisterPatientRequest request) {
        Doctor doctor = doctorRepository.findByIdForUpdate(request.doctorId())
                .filter(Doctor::isActive)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        Patient patient = findOrCreatePatient(request);

        LocalDate today = LocalDate.now(clock);
        int nextToken = visitRepository.findMaxToken(doctor.getId(), today) + 1;

        Visit visit = visitRepository.save(
                new Visit(patient, doctor, today, nextToken));

        Long doctorId = doctor.getId();

        AfterCommit.run(
                () -> queueService.addWaiting(doctorId, today, nextToken));

        return VisitResponse.from(visit);
    }

    @Transactional(readOnly = true)
    public List<VisitResponse> listToday(Long doctorId) {
        return visitRepository
                .findByDoctorIdAndVisitDateOrderByTokenNumberAsc(
                        doctorId, LocalDate.now(clock))
                .stream()
                .map(VisitResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicVisitResponse getPublicView(String publicId) {
        Visit visit = visitRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException("Token not found"));

        Doctor doctor = visit.getDoctor();

        QueueSnapshot snapshot = queueService.snapshot(
                doctor.getId(), visit.getVisitDate());

        int token = visit.getTokenNumber();
        boolean waiting = visit.getStatus() == VisitStatus.WAITING;

        int ahead = waiting ? snapshot.patientsAhead(token) : 0;

        int waitMinutes = waiting
                ? (int) Math.ceil(
                snapshot.estimatedWaitSeconds(token) / 60.0)
                : 0;

        return new PublicVisitResponse(
                token,
                doctor.getName(),
                visit.getStatus(),
                waiting ? ahead + 1 : null,
                ahead,
                waitMinutes,
                snapshot.currentToken(),
                waiting && ahead <= COME_NOW_THRESHOLD
        );
    }

    private Patient findOrCreatePatient(RegisterPatientRequest request) {
        String phone = request.phone() == null
                ? ""
                : request.phone().trim();

        if (!phone.isEmpty()) {
            return patientRepository.findFirstByPhone(phone)
                    .orElseGet(() -> patientRepository.save(
                            new Patient(request.name().trim(), phone)));
        }

        return patientRepository.save(
                new Patient(request.name().trim(), null));
    }
}