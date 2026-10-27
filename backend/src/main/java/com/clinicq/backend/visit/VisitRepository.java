package com.clinicq.backend.visit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    Optional<Visit> findByPublicId(String publicId);

    List<Visit> findByDoctorIdAndVisitDateOrderByTokenNumberAsc(
            Long doctorId, LocalDate visitDate);

    Optional<Visit> findFirstByDoctorIdAndVisitDateAndStatusOrderByTokenNumberAsc(
            Long doctorId, LocalDate visitDate, VisitStatus status);

    List<Visit> findTop10ByDoctorIdAndStatusOrderByFinishedAtDesc(
            Long doctorId, VisitStatus status);

    Optional<Visit> findByDoctorIdAndVisitDateAndTokenNumber(
            Long doctorId, LocalDate visitDate, int tokenNumber);

    @Query("select coalesce(max(v.tokenNumber), 0) from Visit v " +
            "where v.doctor.id = :doctorId and v.visitDate = :date")
    int findMaxToken(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date);

    long countByDoctorIdAndVisitDateAndStatusInAndTokenNumberLessThan(
            Long doctorId,
            LocalDate visitDate,
            Collection<VisitStatus> statuses,
            int tokenNumber);
}