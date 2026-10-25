package com.clinicq.backend.config;

import com.clinicq.backend.user.Role;
import com.clinicq.backend.user.StaffUser;
import com.clinicq.backend.user.StaffUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final StaffUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(StaffUserRepository repository,
                      PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        // DEV ONLY: change these credentials before real deployment
        repository.save(new StaffUser(
                "reception",
                passwordEncoder.encode("reception123"),
                "Front Desk",
                Role.RECEPTIONIST
        ));

        repository.save(new StaffUser(
                "doctor",
                passwordEncoder.encode("doctor123"),
                "Dr. Ramesh",
                Role.DOCTOR
        ));

        log.info("Seeded default dev users: reception / doctor");
    }
}