package com.clinicq.backend.config;

import com.clinicq.backend.doctor.Doctor;
import com.clinicq.backend.doctor.DoctorRepository;
import com.clinicq.backend.user.Role;
import com.clinicq.backend.user.StaffUser;
import com.clinicq.backend.user.StaffUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final StaffUserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            StaffUserRepository userRepository,
            DoctorRepository doctorRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Doctor doctor = doctorRepository.findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> doctorRepository.save(
                        new Doctor("Dr. Ramesh", "General Physician")));

        ensureUser("admin", "admin123", "Administrator", Role.ADMIN, null);
        ensureUser("reception", "reception123", "Front Desk", Role.RECEPTIONIST, null);
        ensureUser("doctor", "doctor123", doctor.getName(), Role.DOCTOR, doctor);
    }

    private void ensureUser(
            String username,
            String password,
            String fullName,
            Role role,
            Doctor doctor) {

        StaffUser user = userRepository.findByUsername(username)
                .orElseGet(StaffUser::new);

        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(role);
        user.setDoctor(doctor);

        userRepository.save(user);
    }
}