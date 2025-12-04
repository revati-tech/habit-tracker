package com.mahajan.habittracker.controller;

import com.mahajan.habittracker.exceptions.HabitAlreadyCompletedException;
import com.mahajan.habittracker.exceptions.HabitCompletionNotFoundException;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.model.HabitCompletion;
import com.mahajan.habittracker.model.User;
import com.mahajan.habittracker.security.JwtAuthFilter;
import com.mahajan.habittracker.service.HabitCompletionService;
import com.mahajan.habittracker.service.HabitService;
import com.mahajan.habittracker.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for {@link HabitCompletionController}.
 */
@WebMvcTest(controllers = HabitCompletionController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class HabitCompletionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HabitCompletionService completionService;

    @MockBean
    private HabitService habitService;

    @MockBean
    private UserService userService;

    private static final Long HABIT_ID = 10L;
    private static final String USER_EMAIL = "test@example.com";

    // ✅ Positive Case: Mark habit completed
    @Test
    @DisplayName("POST /api/habits/{habitId}/completions should mark habit as completed and return 200 OK")
    @WithMockUser(username = USER_EMAIL)
    void testMarkCompletedSuccess() throws Exception {
        User mockUser = User.builder().id(1L).email(USER_EMAIL).build();
        Habit mockHabit = Habit.builder().id(HABIT_ID).name("Exercise").description("Daily workout").build();

        Mockito.when(userService.getUserByEmail(USER_EMAIL)).thenReturn(mockUser);
        Mockito.when(habitService.getHabitByIdForUser(HABIT_ID, mockUser)).thenReturn(mockHabit);

        mockMvc.perform(post("/api/habits/{habitId}/completions", HABIT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Habit marked as completed")));

        Mockito.verify(completionService)
                .markCompleted(eq(mockHabit), eq(mockUser), any(LocalDate.class));
    }

    // ❌ Negative Case: Habit already completed
    @Test
    @DisplayName("POST /api/habits/{habitId}/completions returns 409 Conflict when habit already completed")
    @WithMockUser(username = USER_EMAIL)
    void testMarkCompletedAlreadyExists() throws Exception {
        User mockUser = User.builder().id(1L).email(USER_EMAIL).build();
        Habit mockHabit = Habit.builder().id(HABIT_ID).name("Exercise").build();

        Mockito.when(userService.getUserByEmail(USER_EMAIL)).thenReturn(mockUser);
        Mockito.when(habitService.getHabitByIdForUser(HABIT_ID, mockUser)).thenReturn(mockHabit);
        Mockito.doThrow(new HabitAlreadyCompletedException(HABIT_ID, LocalDate.now().toString()))
                .when(completionService)
                .markCompleted(eq(mockHabit), eq(mockUser), any(LocalDate.class));

        mockMvc.perform(post("/api/habits/{habitId}/completions", HABIT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        Mockito.verify(completionService)
                .markCompleted(eq(mockHabit), eq(mockUser), any(LocalDate.class));
    }

    // ✅ Positive Case: Get completions for habit
    @Test
    @DisplayName("GET /api/habits/{habitId}/completions should return list of completion dates")
    @WithMockUser(username = USER_EMAIL)
    void testGetCompletionsSuccess() throws Exception {
        User mockUser = User.builder().id(1L).email(USER_EMAIL).build();
        Habit mockHabit = Habit.builder().id(HABIT_ID).name("Exercise").description("Workout").build();

        Mockito.when(userService.getUserByEmail(USER_EMAIL)).thenReturn(mockUser);
        Mockito.when(habitService.getHabitByIdForUser(HABIT_ID, mockUser)).thenReturn(mockHabit);


        Mockito.when(completionService.getAllCompletionsForHabit(mockHabit, mockUser))
                .thenReturn(List.of()); // The controller maps entities to DTOs — skip that level for simplicity

        mockMvc.perform(get("/api/habits/{habitId}/completions", HABIT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(userService).getUserByEmail(USER_EMAIL);
        Mockito.verify(habitService).getHabitByIdForUser(HABIT_ID, mockUser);
    }

    // ✅ Positive Case: Unmark completion (delete)
    @Test
    @DisplayName("DELETE /api/habits/{habitId}/completions/{date} should delete completion and return 204 No Content")
    @WithMockUser(username = USER_EMAIL)
    void testUnmarkCompletedSuccess() throws Exception {
        User mockUser = User.builder().id(1L).email(USER_EMAIL).build();
        Habit mockHabit = Habit.builder().id(HABIT_ID).name("Exercise").build();
        String date = "2025-10-21";

        Mockito.when(userService.getUserByEmail(USER_EMAIL)).thenReturn(mockUser);
        Mockito.when(habitService.getHabitByIdForUser(HABIT_ID, mockUser)).thenReturn(mockHabit);

        mockMvc.perform(delete("/api/habits/{habitId}/completions/{date}", HABIT_ID, date)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Mockito.verify(completionService)
                .unmarkCompleted(eq(mockHabit), eq(mockUser), eq(LocalDate.parse(date)));
    }

    // ❌ Negative Case: Unmark completion not found
    @Test
    @DisplayName("DELETE /api/habits/{habitId}/completions/{date} returns 404 when completion not found")
    @WithMockUser(username = USER_EMAIL)
    void testUnmarkCompletedNotFound() throws Exception {
        User mockUser = User.builder().id(1L).email(USER_EMAIL).build();
        Habit mockHabit = Habit.builder().id(HABIT_ID).name("Exercise").build();
        String date = "2025-10-21";

        Mockito.when(userService.getUserByEmail(USER_EMAIL)).thenReturn(mockUser);
        Mockito.when(habitService.getHabitByIdForUser(HABIT_ID, mockUser)).thenReturn(mockHabit);
        Mockito.doThrow(new HabitCompletionNotFoundException(HABIT_ID, date))
                .when(completionService)
                .unmarkCompleted(eq(mockHabit), eq(mockUser), eq(LocalDate.parse(date)));

        mockMvc.perform(delete("/api/habits/{habitId}/completions/{date}", HABIT_ID, date)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(completionService)
                .unmarkCompleted(eq(mockHabit), eq(mockUser), eq(LocalDate.parse(date)));
    }

    // ✅ Positive Case: Get completions by date with date parameter
    @Test
    @DisplayName("GET /api/habits/completions?date=2025-12-03 should return list of completions for that date")
    @WithMockUser(username = USER_EMAIL)
    void testGetCompletionsByDateWithDateParameter() throws Exception {
        User mockUser = User.builder().id(1L).email(USER_EMAIL).build();
        String date = "2025-12-03";
        LocalDate completionDate = LocalDate.parse(date);

        Habit habit1 = Habit.builder().id(4L).name("Workout").description("Daily exercise").build();
        Habit habit2 = Habit.builder().id(7L).name("Read").description("Reading books").build();

        HabitCompletion completion1 = HabitCompletion.builder()
                .id(1L)
                .habit(habit1)
                .user(mockUser)
                .completionDate(completionDate)
                .build();

        HabitCompletion completion2 = HabitCompletion.builder()
                .id(2L)
                .habit(habit2)
                .user(mockUser)
                .completionDate(completionDate)
                .build();

        Mockito.when(userService.getUserByEmail(USER_EMAIL)).thenReturn(mockUser);
        Mockito.when(completionService.getCompletionsByDate(mockUser, completionDate))
                .thenReturn(List.of(completion1, completion2));

        mockMvc.perform(get("/api/habits/completions")
                        .param("date", date)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].habitId").value(4))
                .andExpect(jsonPath("$[0].habitName").value("Workout"))
                .andExpect(jsonPath("$[0].completionDate").value("2025-12-03"))
                .andExpect(jsonPath("$[1].habitId").value(7))
                .andExpect(jsonPath("$[1].habitName").value("Read"))
                .andExpect(jsonPath("$[1].completionDate").value("2025-12-03"));

        Mockito.verify(userService).getUserByEmail(USER_EMAIL);
        Mockito.verify(completionService).getCompletionsByDate(mockUser, completionDate);
    }

    // ✅ Positive Case: Get completions by date without date parameter (defaults to today)
    @Test
    @DisplayName("GET /api/habits/completions should return list of completions for today when date not provided")
    @WithMockUser(username = USER_EMAIL)
    void testGetCompletionsByDateWithoutDateParameter() throws Exception {
        User mockUser = User.builder().id(1L).email(USER_EMAIL).build();
        LocalDate today = LocalDate.now();

        Habit habit1 = Habit.builder().id(4L).name("Workout").description("Daily exercise").build();

        HabitCompletion completion1 = HabitCompletion.builder()
                .id(1L)
                .habit(habit1)
                .user(mockUser)
                .completionDate(today)
                .build();

        Mockito.when(userService.getUserByEmail(USER_EMAIL)).thenReturn(mockUser);
        Mockito.when(completionService.getCompletionsByDate(mockUser, today))
                .thenReturn(List.of(completion1));

        mockMvc.perform(get("/api/habits/completions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].habitId").value(4))
                .andExpect(jsonPath("$[0].habitName").value("Workout"))
                .andExpect(jsonPath("$[0].completionDate").value(today.toString()));

        Mockito.verify(userService).getUserByEmail(USER_EMAIL);
        Mockito.verify(completionService).getCompletionsByDate(mockUser, today);
    }

    // ✅ Positive Case: Get completions by date returns empty list when no completions
    @Test
    @DisplayName("GET /api/habits/completions?date=2025-12-03 should return empty list when no completions exist")
    @WithMockUser(username = USER_EMAIL)
    void testGetCompletionsByDateEmptyList() throws Exception {
        User mockUser = User.builder().id(1L).email(USER_EMAIL).build();
        String date = "2025-12-03";
        LocalDate completionDate = LocalDate.parse(date);

        Mockito.when(userService.getUserByEmail(USER_EMAIL)).thenReturn(mockUser);
        Mockito.when(completionService.getCompletionsByDate(mockUser, completionDate))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/habits/completions")
                        .param("date", date)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        Mockito.verify(userService).getUserByEmail(USER_EMAIL);
        Mockito.verify(completionService).getCompletionsByDate(mockUser, completionDate);
    }
}
