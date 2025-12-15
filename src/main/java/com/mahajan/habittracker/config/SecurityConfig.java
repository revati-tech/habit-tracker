package com.mahajan.habittracker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahajan.habittracker.exceptions.ErrorResponse;
import com.mahajan.habittracker.exceptions.GlobalExceptionHandler;
import com.mahajan.habittracker.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final GlobalExceptionHandler exceptionHandler;
    private final ObjectMapper objectMapper;
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * Defines the PasswordEncoder bean used across the application.
     * BCrypt is the recommended encoder for production.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the AuthenticationManager by letting Spring Boot
     * wire it automatically using the available UserAuthService + PasswordEncoder.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Main security filter chain.
     * For now: disable CSRF, allow all requests.
     * Later: restrict endpoints by role.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health").permitAll()        // health check is public
                        .requestMatchers("/api/auth/**").permitAll()   // signup/login are public
                        .anyRequest().authenticated()                    // everything else requires auth
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            // delegate to GlobalExceptionHandler
                            ResponseEntity<ErrorResponse> entity =
                                    exceptionHandler.buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", request);
                            writeResponse(response, entity);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            ResponseEntity<ErrorResponse> entity =
                                    exceptionHandler.buildResponse(HttpStatus.FORBIDDEN, "Forbidden", request);
                            writeResponse(response, entity);
                        })
                )
                // disable default login mechanisms, since you're using JWT
                .formLogin(AbstractAuthenticationFilterConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // register your custom JWT filter
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();

    }

    private void writeResponse(HttpServletResponse response,
                               ResponseEntity<ErrorResponse> entity) throws IOException {
        response.setStatus(entity.getStatusCode().value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), entity.getBody());
    }
}
