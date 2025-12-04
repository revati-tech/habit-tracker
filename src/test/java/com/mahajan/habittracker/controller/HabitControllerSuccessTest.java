package com.mahajan.habittracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahajan.habittracker.dto.HabitRequest;
import com.mahajan.habittracker.dto.StreakResult;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.model.User;
import com.mahajan.habittracker.security.JwtAuthFilter;
import com.mahajan.habittracker.service.HabitService;
import com.mahajan.habittracker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = HabitController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class HabitControllerSuccessTest {

    private static final String BASE_URL = "/api/habits";

    private static final String BASE_URL_WITH_ID = "/api/habits/{habitId}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private HabitService habitService;

    @Autowired
    private ObjectMapper objectMapper;

    private Habit testHabit;

    private User testUser;

    @BeforeEach
    void setUp() {
        testHabit = Habit.builder().id(100L).name("Exercise").description("Daily workout").build();

        // Set up a fake authenticated user
        testUser = User.builder().id(1L).email("test@example.com").build();

        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
    }

    private Habit addHabitAndReturn() throws Exception {
        HabitRequest request = HabitRequest.builder()
                .name(testHabit.getName())
                .description(testHabit.getDescription())
                .build();
        when(habitService.createHabitForUser(any(Habit.class), eq(testUser))).thenReturn(testHabit);
        String response = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

       return objectMapper.readValue(response, Habit.class);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testCreateHabit() throws Exception {
        HabitRequest request = HabitRequest.builder()
                .name(testHabit.getName())
                .description(testHabit.getDescription())
                .build();
        when(habitService.createHabitForUser(any(Habit.class), eq(testUser))).thenReturn(testHabit);
     mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/habits/100"))
                .andExpect(jsonPath("$.name").value("Exercise"))
                .andExpect(jsonPath("$.description").value("Daily workout"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetAllHabits() throws Exception {

        Habit habit2 = Habit.builder().name("Meditation").description("Daily meditation").build();

        when(habitService.getHabitsForUser(any(User.class)))
                .thenReturn(List.of(testHabit, habit2));
        when(habitService.calculateStreaksForHabit(any(Habit.class), any(User.class)))
                .thenReturn(new StreakResult(0, 0));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$", hasSize(2))))
                .andExpect(jsonPath("$[0].name").value("Exercise"))
                .andExpect(jsonPath("$[0].description").value("Daily workout"))
                .andExpect(jsonPath("$[1].name").value("Meditation"))
                .andExpect(jsonPath("$[1].description").value("Daily meditation"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetHabitById() throws Exception {
        when(habitService.getHabitByIdForUser(any(Long.class), any(User.class))).thenReturn(testHabit);
        when(habitService.calculateStreaksForHabit(any(Habit.class), any(User.class)))
                .thenReturn(new StreakResult(0, 0));
        mockMvc.perform(get(BASE_URL_WITH_ID, 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("Exercise"))
                .andExpect(jsonPath("description").value("Daily workout"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testDeleteHabit() throws Exception {
        Habit savedHabit = addHabitAndReturn();
        mockMvc.perform(delete(BASE_URL_WITH_ID, 100L))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(BASE_URL, testUser.getId(), savedHabit.getId()))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$", hasSize(0))));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testUpdateHabit() throws Exception {
        HabitRequest habitRequest = HabitRequest.builder().name("Updated Name").description("Updated Description").build();
        Habit updatedHabit = Habit.builder().id(100L).name("Updated Name").description("Updated Description").build();
        when(habitService.updateHabitForUser(any(Habit.class), any(User.class)))
                .thenReturn(updatedHabit);
        when(habitService.calculateStreaksForHabit(any(Habit.class), any(User.class)))
                .thenReturn(new StreakResult(0, 0));
        mockMvc.perform(put(BASE_URL_WITH_ID, 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(habitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }
}
