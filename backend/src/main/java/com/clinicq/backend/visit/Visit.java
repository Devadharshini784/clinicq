package com.clinicq.backend.visit;

import com.clinicq.backend.doctor.Doctor;
import com.clinicq.backend.patient.Patient;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "visits",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_visit_doctor_date_token",
                columnNames = {"doctor_id", "visit_date", "token_number"}
        ),
        indexes = @Index(
                name = "idx_visit_doctor_date",
                columnList = "doctor_id, visit_date"
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String publicId = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDate visitDate;

    @Column(nullable = false)
    private int tokenNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisitStatus status = VisitStatus.WAITING;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant startedAt;

    private Instant finishedAt;

    public Visit(
            Patient patient,
            Doctor doctor,
            LocalDate visitDate,
            int tokenNumber
    ) {
        this.patient = patient;
        this.doctor = doctor;
        this.visitDate = visitDate;
        this.tokenNumber = tokenNumber;
    }
}