package com.clinicq.backend.auth;

import com.clinicq.backend.user.StaffUser;
import com.clinicq.backend.user.StaffUserRepository;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final StaffUserRepository userRepository;
    private final JwtService jwtService;

    public AuthController(
            AuthenticationManager authenticationManager,
            StaffUserRepository userRepository,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(), request.password()));

        StaffUser user = userRepository.findByUsername(request.username())
                .orElseThrow();

        String token = jwtService.generateToken(
                user.getUsername(), user.getRole());

        Long doctorId = user.getDoctor() == null
                ? null
                : user.getDoctor().getId();

        return new LoginResponse(
                token,
                user.getUsername(),
                user.getRole().name(),
                doctorId
        );
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        return Map.of(
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities().toString());
    }
}