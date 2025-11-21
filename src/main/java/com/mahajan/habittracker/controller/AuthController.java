package com.mahajan.habittracker.controller;

import com.mahajan.habittracker.dto.AuthResponse;
import com.mahajan.habittracker.dto.LoginRequest;
import com.mahajan.habittracker.dto.SignupRequest;
import com.mahajan.habittracker.exceptions.EmailAlreadyExistsException;
import com.mahajan.habittracker.model.User;
import com.mahajan.habittracker.service.UserService;
import com.mahajan.habittracker.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        userService.createUser(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            String token = jwtUtil.generateToken(request.getEmail());
            log.info("Login success userId={}", request.getEmail());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (Exception e) {
            log.warn("Login failed for userId={}: {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }
}
