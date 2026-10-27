package com.clinicq.backend.visit;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    // Register a patient and generate a token
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VisitResponse register(
            @Valid @RequestBody RegisterPatientRequest request) {
        return visitService.register(request);
    }

    // Get today's queue for a particular doctor
    @GetMapping("/today")
    public List<VisitResponse> listToday(
            @RequestParam Long doctorId) {
        return visitService.listToday(doctorId);
    }
}