package com.mahajan.habittracker.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    // Inject test values manually
    {
        jwtUtil.setSecret("my-very-secure-secret-key-1234567890");
        jwtUtil.setExpiration(3600000);
    }

    @Test
    void testGenerateAndExtractEmail() {
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email);

        assertNotNull(token);
        String extractedEmail = jwtUtil.extractEmail(token);
        assertEquals(email, extractedEmail);
    }
}
