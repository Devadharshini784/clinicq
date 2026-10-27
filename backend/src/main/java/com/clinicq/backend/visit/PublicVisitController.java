package com.clinicq.backend.visit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/visits")
public class PublicVisitController {

    private final VisitService visitService;

    public PublicVisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    // Allow patients to check their token without logging in
    @GetMapping("/{publicId}")
    public PublicVisitResponse getPublicView(
            @PathVariable String publicId) {
        return visitService.getPublicView(publicId);
    }
}
