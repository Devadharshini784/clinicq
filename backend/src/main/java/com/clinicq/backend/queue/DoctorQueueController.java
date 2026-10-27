package com.clinicq.backend.queue;

import com.clinicq.backend.config.NotFoundException;
import com.clinicq.backend.user.StaffUser;
import com.clinicq.backend.user.StaffUserRepository;
import com.clinicq.backend.visit.VisitService;
import com.clinicq.backend.visit.VisitResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctor/queue")
public class DoctorQueueController {

    private final StaffUserRepository userRepository;
    private final QueueService queueService;
    private final QueueControlService queueControlService;
    private final VisitService visitService;
    private final Clock clock;

    public DoctorQueueController(
            StaffUserRepository userRepository,
            QueueService queueService,
            QueueControlService queueControlService,
            VisitService visitService,
            Clock clock) {
        this.userRepository = userRepository;
        this.queueService = queueService;
        this.queueControlService = queueControlService;
        this.visitService = visitService;
        this.clock = clock;
    }

    @GetMapping
    public DoctorQueueResponse getQueue(Authentication authentication) {
        StaffUser user = getDoctorUser(authentication);
        Long doctorId = user.getDoctor().getId();
        LocalDate today = LocalDate.now(clock);

        QueueSnapshot snapshot = queueService.snapshotToday(doctorId);
        List<VisitResponse> visits = visitService.listToday(doctorId);

        return new DoctorQueueResponse(snapshot, visits);
    }

    @PostMapping("/next")
    public void next(Authentication authentication) {
        queueControlService.next(getDoctorId(authentication));
    }

    @PostMapping("/skip")
    public void skip(Authentication authentication) {
        queueControlService.skip(getDoctorId(authentication));
    }

    @PostMapping("/done")
    public void done(Authentication authentication) {
        queueControlService.done(getDoctorId(authentication));
    }

    private Long getDoctorId(Authentication authentication) {
        return getDoctorUser(authentication).getDoctor().getId();
    }

    private StaffUser getDoctorUser(Authentication authentication) {
        StaffUser user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new NotFoundException("Staff user not found"));

        if (user.getRole() == null || user.getDoctor() == null) {
            throw new NotFoundException("No doctor is linked to this account");
        }

        return user;
    }
}