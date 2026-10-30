package com.clinicq.backend.security;

import com.clinicq.backend.user.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET =
            "clinicq-test-secret-key-at-least-32-characters-long";

    private static final long TTL_MILLIS = 60_000;

    private JwtService createService(String secret, long ttlMillis) {
        return new JwtService(secret, ttlMillis);
    }

    @Test
    void generatedTokenCanBeParsed() {
        JwtService jwtService = createService(SECRET, TTL_MILLIS);

        String token = jwtService.generateToken(
                "reception", Role.RECEPTIONIST
        );

        Claims claims = jwtService.parse(token);

        assertEquals("reception", claims.getSubject());
        assertEquals("RECEPTIONIST", claims.get("role", String.class));
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService jwtService = createService(SECRET, TTL_MILLIS);

        String token = jwtService.generateToken(
                "reception", Role.RECEPTIONIST
        );

        assertThrows(RuntimeException.class,
                () -> jwtService.parse(token + "tampered"));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtService originalService = createService(SECRET, TTL_MILLIS);

        JwtService differentService = createService(
                "another-test-secret-key-at-least-32-characters-long",
                TTL_MILLIS
        );

        String token = originalService.generateToken(
                "reception", Role.RECEPTIONIST
        );

        assertThrows(RuntimeException.class,
                () -> differentService.parse(token));
    }

    @Test
    void expiredTokenIsRejected() throws InterruptedException {
        JwtService jwtService = createService(SECRET, 1);

        String token = jwtService.generateToken(
                "reception", Role.RECEPTIONIST
        );

        Thread.sleep(50);

        assertThrows(RuntimeException.class,
                () -> jwtService.parse(token));
    }
}
