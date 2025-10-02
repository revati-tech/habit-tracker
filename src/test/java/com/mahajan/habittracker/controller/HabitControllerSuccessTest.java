package com.mahajan.habittracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahajan.habittracker.dto.HabitRequest;
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
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(testUser.getEmail(), null));

        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
    }

    private Habit addHabitAndReturn() throws Exception {
        HabitRequest request = HabitRequest.builder()
                .name(testHabit.getName())
                .description(testHabit.getDescription())
                .build();
        String response = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

       return objectMapper.readValue(response, Habit.class);
    }

    @Test
    void testCreateHabit() throws Exception {
        HabitRequest request = HabitRequest.builder()
                .name(testHabit.getName())
                .description(testHabit.getDescription())
                .build();
        when(habitService.createHabitForUser(testHabit, testUser)).thenReturn(testHabit);
     mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Exercise"))
                .andExpect(jsonPath("$.description").value("Daily workout"));
    }

    @Test
    void testGetAllHabits() throws Exception {

        Habit habit2 = Habit.builder().name("Meditation").description("Daily meditation").build();

        when(habitService.getHabitsForUser(any(User.class)))
                .thenReturn(List.of(testHabit, habit2));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$", hasSize(2))))
                .andExpect(jsonPath("$[0].name").value("Exercise"))
                .andExpect(jsonPath("$[0].description").value("Daily workout"))
                .andExpect(jsonPath("$[1].name").value("Meditation"))
                .andExpect(jsonPath("$[1].description").value("Daily meditation"));
    }

    @Test
    void testGetHabitById() throws Exception {
        when(habitService.getHabitByIdForUser(any(Long.class), any(User.class))).thenReturn(testHabit);
        mockMvc.perform(get(BASE_URL_WITH_ID, 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("Exercise"))
                .andExpect(jsonPath("description").value("Daily workout"));
    }

    @Test
    void testDeleteHabit() throws Exception {
        Habit savedHabit = addHabitAndReturn();
        mockMvc.perform(delete(BASE_URL_WITH_ID, 100L))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(BASE_URL, testUser.getId(), savedHabit.getId()))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$", hasSize(0))));
    }

    @Test
    void testUpdateHabit() throws Exception {
        HabitRequest habitRequest = HabitRequest.builder().name("Updated Name").description("Updated Description").build();
        when(habitService.updateHabitForUser(any(Habit.class), any(User.class)))
                .thenReturn(Habit.builder().id(100L).name("Updated Name").description("Updated Description").build());
        mockMvc.perform(put(BASE_URL_WITH_ID, 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(habitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }
}
