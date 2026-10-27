package com.clinicq.backend.visit;

import com.clinicq.backend.config.NotFoundException;
import com.clinicq.backend.doctor.Doctor;
import com.clinicq.backend.doctor.DoctorRepository;
import com.clinicq.backend.patient.Patient;
import com.clinicq.backend.patient.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

@Service
public class VisitService {

    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final Clock clock;

    public VisitService(
            VisitRepository visitRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            Clock clock) {
        this.visitRepository = visitRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.clock = clock;
    }

    @Transactional
    public VisitResponse register(RegisterPatientRequest request) {

        Doctor doctor = doctorRepository.findByIdForUpdate(request.doctorId())
                .filter(Doctor::isActive)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        Patient patient = findOrCreatePatient(request);

        LocalDate today = LocalDate.now(clock);

        int nextToken =
                visitRepository.findMaxToken(doctor.getId(), today) + 1;

        Visit visit = visitRepository.save(
                new Visit(patient, doctor, today, nextToken));

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

        long ahead = 0;

        if (visit.getStatus() == VisitStatus.WAITING) {
            ahead = visitRepository
                    .countByDoctorIdAndVisitDateAndStatusInAndTokenNumberLessThan(
                            visit.getDoctor().getId(),
                            visit.getVisitDate(),
                            EnumSet.of(
                                    VisitStatus.WAITING,
                                    VisitStatus.IN_PROGRESS),
                            visit.getTokenNumber());
        }

        return new PublicVisitResponse(
                visit.getTokenNumber(),
                visit.getDoctor().getName(),
                visit.getStatus(),
                ahead);
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