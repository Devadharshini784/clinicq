package com.clinicq.backend.doctor;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorRepository doctorRepository;

    public DoctorController(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Doctor create(@Valid @RequestBody DoctorRequest request) {
        return doctorRepository.save(
                new Doctor(request.name(), request.specialization())
        );
    }

    @GetMapping
    public List<Doctor> list() {
        return doctorRepository.findAll();
    }
}