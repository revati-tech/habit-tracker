package com.mahajan.habittracker.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahajan.habittracker.dto.HabitRequest;
import com.mahajan.habittracker.dto.LoginRequest;
import com.mahajan.habittracker.dto.SignupRequest;
import com.mahajan.habittracker.model.HabitCompletion;
import com.mahajan.habittracker.repository.HabitCompletionRepository;
import com.mahajan.habittracker.repository.HabitRepository;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests for habit completion endpoints.
 * Verifies mark/unmark completion, conflict, and not found flows.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class HabitCompletionsIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private HabitRepository habitRepository;
    @Autowired private HabitCompletionRepository completionRepository;

    private String token;
    private Long habitId;

    @BeforeEach
    void setup() throws Exception {
        completionRepository.deleteAll();
        habitRepository.deleteAll();
        userRepository.deleteAll();

        token = signUpAndLogin();
        habitId = createHabit("Exercise", "Morning run");
    }

    // -------------------------------------------------------------------------
    // 🔹 Completion Flow Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Mark habit completed → Verify created → Get completions → Unmark → Verify removed")
    void completeAndUnmarkFlow() throws Exception {
        LocalDate today = LocalDate.now();

        // 1. Mark completed
        mockMvc.perform(post("/api/habits/{id}/completions", habitId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        List<HabitCompletion> completionsAfterAdd = completionRepository.findAll();
        assertThat(completionsAfterAdd).hasSize(1);
        assertThat(completionsAfterAdd.get(0).getCompletionDate()).isEqualTo(today);

        // 2. Get completions (should list today)
        mockMvc.perform(get("/api/habits/{id}/completions", habitId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // 3. Unmark completion
        mockMvc.perform(delete("/api/habits/{id}/completions/{date}", habitId, today)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        List<HabitCompletion> completionsAfterDelete = completionRepository.findAll();
        assertThat(completionsAfterDelete).isEmpty();
    }

    @Test
    @DisplayName("Mark completion twice → Second call should return 409 Conflict")
    void duplicateCompletionShouldReturnConflict() throws Exception {
        // First call succeeds
        mockMvc.perform(post("/api/habits/{id}/completions", habitId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Second call → Conflict
        mockMvc.perform(post("/api/habits/{id}/completions", habitId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());

        assertThat(completionRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Unmark non-existent completion should return 404 Not Found")
    void unmarkNonExistentCompletion() throws Exception {
        String date = LocalDate.now().minusDays(1).toString();

        mockMvc.perform(delete("/api/habits/{id}/completions/{date}", habitId, date)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Mark completion without JWT should return 401 Unauthorized")
    void markCompletionWithoutJwtShouldFail() throws Exception {
        mockMvc.perform(post("/api/habits/{id}/completions", habitId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // 🔹 Get Completions by Date Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/habits/completions?date=... should return all completions for that date")
    void getCompletionsByDateWithDateParameter() throws Exception {
        // Create multiple habits
        Long habitId1 = createHabit("Workout", "Daily exercise");
        Long habitId2 = createHabit("Read", "Reading books");
        String date = "2025-12-03";

        // Mark both habits as completed on the same date
        mockMvc.perform(post("/api/habits/{id}/completions", habitId1)
                        .header("Authorization", "Bearer " + token)
                        .param("date", date))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/habits/{id}/completions", habitId2)
                        .header("Authorization", "Bearer " + token)
                        .param("date", date))
                .andExpect(status().isOk());

        // Get completions for that date
        mockMvc.perform(get("/api/habits/completions")
                        .header("Authorization", "Bearer " + token)
                        .param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].habitId").exists())
                .andExpect(jsonPath("$[0].habitName").exists())
                .andExpect(jsonPath("$[0].completionDate").value(date))
                .andExpect(jsonPath("$[1].habitId").exists())
                .andExpect(jsonPath("$[1].habitName").exists())
                .andExpect(jsonPath("$[1].completionDate").value(date));
    }

    @Test
    @DisplayName("GET /api/habits/completions without date should return completions for today")
    void getCompletionsByDateWithoutDateParameter() throws Exception {
        LocalDate today = LocalDate.now();

        // Mark habit as completed (defaults to today)
        mockMvc.perform(post("/api/habits/{id}/completions", habitId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Get completions without date parameter (should default to today)
        mockMvc.perform(get("/api/habits/completions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].habitId").value(habitId.intValue()))
                .andExpect(jsonPath("$[0].completionDate").value(today.toString()));
    }

    @Test
    @DisplayName("GET /api/habits/completions?date=... should return empty list when no completions exist")
    void getCompletionsByDateWithNoCompletions() throws Exception {
        String date = "2025-12-03";

        mockMvc.perform(get("/api/habits/completions")
                        .header("Authorization", "Bearer " + token)
                        .param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/habits/completions should only return completions for the specified date")
    void getCompletionsByDateShouldFilterByDate() throws Exception {
        String date1 = "2025-12-03";
        String date2 = "2025-12-04";

        // Mark habit as completed on date1
        mockMvc.perform(post("/api/habits/{id}/completions", habitId)
                        .header("Authorization", "Bearer " + token)
                        .param("date", date1))
                .andExpect(status().isOk());

        // Get completions for date2 (should be empty)
        mockMvc.perform(get("/api/habits/completions")
                        .header("Authorization", "Bearer " + token)
                        .param("date", date2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Get completions for date1 (should have one)
        mockMvc.perform(get("/api/habits/completions")
                        .header("Authorization", "Bearer " + token)
                        .param("date", date1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].completionDate").value(date1));
    }

    @Test
    @DisplayName("GET /api/habits/completions without JWT should return 401 Unauthorized")
    void getCompletionsByDateWithoutJwtShouldFail() throws Exception {
        mockMvc.perform(get("/api/habits/completions")
                        .param("date", "2025-12-03"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // 🔹 Helper methods (reused from HabitsIntegrationTest)
    // -------------------------------------------------------------------------

    private String signUpAndLogin() throws Exception {
        signUp();
        LoginRequest login = LoginRequest.builder()
                .email("bob@example.com").password("password123").build();

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
                .email("bob@example.com").password("password123").build();

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
