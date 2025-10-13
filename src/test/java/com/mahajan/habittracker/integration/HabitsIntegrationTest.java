package com.mahajan.habittracker.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahajan.habittracker.dto.HabitRequest;
import com.mahajan.habittracker.dto.LoginRequest;
import com.mahajan.habittracker.dto.SignupRequest;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.repository.HabitRepository;
import com.mahajan.habittracker.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests for authentication + habit endpoints.
 * These tests use real JWTs, H2 DB, and MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class HabitsIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private HabitRepository habitRepository;

    private String token; // Reusable JWT for tests

    @BeforeEach
    void setup() throws Exception {
        userRepository.deleteAll();
        habitRepository.deleteAll();
        token = signUpAndLogin();
    }

    // -------------------------------------------------------------------------
    // 🔹 Habit Flow Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Create habit → Get all habits → Get by ID")
    void createAndRetrieveHabits() throws Exception {
        HabitRequest habitReq = HabitRequest.builder()
                .name("Exercise")
                .description("Morning run")
                .build();

        // 1. Create Habit
        String createResponse = mockMvc.perform(post("/api/habits")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(habitReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Exercise"))
                .andReturn()
                .getResponse().getContentAsString();

        Long habitId = objectMapper.readTree(createResponse).get("id").asLong();

        // 2. Get All Habits
        mockMvc.perform(get("/api/habits")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Exercise"));

        // 3. Get Single Habit by ID
        mockMvc.perform(get("/api/habits/{id}", habitId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Exercise"))
                .andExpect(jsonPath("$.description").value("Morning run"));
    }

    @Test
    @DisplayName("Update existing habit → Confirm changes persisted")
    void updateHabitSuccessfully() throws Exception {
        Long habitId = createHabit("Reading", "Read 30 mins");

        HabitRequest update = HabitRequest.builder()
                .name("Read Books")
                .description("Read 45 mins daily")
                .build();

        mockMvc.perform(put("/api/habits/{id}", habitId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Read Books"))
                .andExpect(jsonPath("$.description").value("Read 45 mins daily"));

        // Verify persisted
        List<Habit> habits = habitRepository.findAll();
        assertThat(habits).hasSize(1);
        assertThat(habits.get(0).getName()).isEqualTo("Read Books");
    }

    @Test
    @DisplayName("Delete habit → Should no longer appear in list")
    void deleteHabit() throws Exception {
        Long habitId = createHabit("Meditation", "Evening session");

        mockMvc.perform(delete("/api/habits/{id}", habitId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/habits")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Create habit without JWT should return 401")
    void createHabitWithoutJwtShouldFail() throws Exception {
        HabitRequest habitReq = HabitRequest.builder()
                .name("Jogging")
                .description("Morning jog")
                .build();

        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(habitReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Create habit with invalid body should return 400")
    void createHabitWithInvalidBody() throws Exception {
        HabitRequest invalid = HabitRequest.builder()
                .description("No name field")
                .build();

        mockMvc.perform(post("/api/habits")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // 🔹 Helper methods
    // -------------------------------------------------------------------------

    private String signUpAndLogin() throws Exception {
        signUp();
        LoginRequest login = LoginRequest.builder()
                .email("alice@example.com").password("password123").build();

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }

    private void signUp() throws Exception {
        SignupRequest signupRequest = SignupRequest.builder()
                .email("alice@example.com").password("password123").build();

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());
    }

    private Long createHabit(String name, String description) throws Exception {
        HabitRequest req = HabitRequest.builder()
                .name(name).description(description).build();

        String response = mockMvc.perform(post("/api/habits")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }
}
