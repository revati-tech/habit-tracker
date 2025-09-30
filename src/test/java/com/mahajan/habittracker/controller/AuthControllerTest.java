package com.mahajan.habittracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahajan.habittracker.config.SecurityTestConfig;
import com.mahajan.habittracker.dto.LoginRequest;
import com.mahajan.habittracker.dto.SignupRequest;
import com.mahajan.habittracker.model.User;
import com.mahajan.habittracker.service.UserService;
import com.mahajan.habittracker.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(SecurityTestConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtil jwtUtil;

    @Nested
    @DisplayName("Signup")
    class SignupTests {

        @Test
        @DisplayName("should register new user")
        void signupSuccess() throws Exception {
            SignupRequest request = new SignupRequest();
            request.setEmail("alice@example.com");
            request.setPassword("password123");

            Mockito.when(userService.existsByEmail("alice@example.com")).thenReturn(false);
            Mockito.when(userService.createUser(any(User.class))).thenReturn(new User());

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("User registered successfully"));
        }

        @Test
        @DisplayName("should fail if email already exists")
        void signupDuplicateEmail() throws Exception {
            SignupRequest request = new SignupRequest();
            request.setEmail("bob@example.com");
            request.setPassword("password123");

            Mockito.when(userService.existsByEmail("bob@example.com")).thenReturn(true);

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Email already in use: bob@example.com"));
        }
    }

    @Nested
    @DisplayName("Login")
    class LoginTests {

        @Test
        @DisplayName("should return JWT token when credentials are valid")
        void loginSuccess() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("charlie@example.com");
            request.setPassword("password123");

            // Return a dummy Authentication object
            Authentication mockAuth = Mockito.mock(Authentication.class);
            Mockito.when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
            Mockito.when(jwtUtil.generateToken(eq("charlie@example.com"))).thenReturn("fake-jwt-token");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("fake-jwt-token"));
        }

        @Test
        @DisplayName("should return 401 when credentials are invalid")
        void loginFailure() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("dave@example.com");
            request.setPassword("wrongpassword");

            Mockito.doThrow(new BadCredentialsException("Invalid email or password"))
                    .when(authenticationManager).authenticate(any());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid email or password"));
        }
    }
}
