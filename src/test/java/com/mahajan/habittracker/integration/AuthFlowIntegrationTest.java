package com.mahajan.habittracker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahajan.habittracker.dto.LoginRequest;
import com.mahajan.habittracker.dto.SignupRequest;
import com.mahajan.habittracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Signup → Login → Access protected endpoint with JWT")
    void fullAuthFlow() throws Exception {
        // 1. Signup a new user (using DTO, not entity)
        SignupRequest signupRequest =  SignupRequest.builder()
                .email("alice@example.com").password("password123").build();
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));

        assertThat(userRepository.findByEmail("alice@example.com")).isPresent();

        // 2. Login with correct credentials
        LoginRequest loginRequest = LoginRequest.builder()
                .email("alice@example.com").password("password123").build();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();
        assertThat(token).isNotBlank();

        // 3. Access protected endpoint with token
       mockMvc.perform(get("/api/habits")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()); // assumes /api/habits exists and is secured

        // 4. Access protected endpoint without token should fail
        mockMvc.perform(get("/api/habits"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login with wrong password should return 401")
    void loginWithWrongPassword() throws Exception {
        signup("bob@example.com", "strongPass");

        LoginRequest loginRequest = LoginRequest.builder()
                .email("bob@example.com").password("wrongPass").build();
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Signup with duplicate email should return 409")
    void signupDuplicateEmail() throws Exception {
        signup("alice@example.com", "password123");

        SignupRequest duplicate = SignupRequest.builder()
                .email("alice@example.com").password("password123").build();
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("Access protected endpoint with malformed JWT should return 401")
    void accessWithMalformedJwt() throws Exception {
        mockMvc.perform(get("/api/habits")
                        .header("Authorization", "Bearer not_a_jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    private void signup(String email, String password) throws Exception {
        SignupRequest signupRequest = SignupRequest.builder()
                .email(email).password(password).build();

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }
}
