package com.mahajan.habittracker.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Nested
    @DisplayName("PasswordEncoder bean")
    class PasswordEncoderBean {

        @Test
        @DisplayName("uses BCryptPasswordEncoder")
        void usesBCryptPasswordEncoder() {
            assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
        }

        @Test
        @DisplayName("encodes raw password to a non-blank value")
        void encodesPassword() {
            String raw = "secret123";
            String encoded = passwordEncoder.encode(raw);

            assertThat(encoded).isNotBlank();
            assertThat(passwordEncoder.matches(raw, encoded)).isTrue();
        }
    }

    @Nested
    @DisplayName("AuthenticationManager bean")
    class AuthenticationManagerBean {

        @Test
        @DisplayName("is available in Spring context")
        void isAvailable() {
            assertThat(authenticationManager).isNotNull();
        }
    }

    @Nested
    @DisplayName("SecurityFilterChain bean")
    class SecurityFilterChainBean {

        @Test
        @DisplayName("is available in Spring context")
        void isAvailable() {
            assertThat(securityFilterChain).isNotNull();
        }
    }
}
