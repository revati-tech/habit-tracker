package com.mahajan.habittracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahajan.habittracker.dto.HabitRequest;
import com.mahajan.habittracker.dto.UserRequest;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.model.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HabitControllerSuccessTest {

    private static final String BASE_URL = "/api/users/{userId}/habits";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Habit testHabit;

    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        // create a user dynamically before each test
        UserRequest userRequest = UserRequest.builder().email("test@test.com").build();
        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        testUser = objectMapper.readValue(response, User.class);

        testHabit = Habit.builder().name("Exercise").description("Daily workout").build();
    }

    private Habit addHabitAndReturn() throws Exception {
        String response = mockMvc.perform(post(BASE_URL, testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testHabit)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

       Habit habit = objectMapper.readValue(response, Habit.class);
       habit.setUser(testUser);
       return habit;
    }

    @Test
    void testCreateHabit() throws Exception {
     mockMvc.perform(post(BASE_URL, testUser.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testHabit))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Exercise"))
                .andExpect(jsonPath("$.description").value("Daily workout"));
    }

    @Test
    void testGetAllHabits() throws Exception {
        addHabitAndReturn();
        mockMvc.perform(get(BASE_URL, testUser.getId()))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$", hasSize(1))))
                .andExpect(jsonPath("$[0].name").value("Exercise"))
                .andExpect(jsonPath("$[0].description").value("Daily workout"));
    }

    @Test
    void testGetHabitById() throws Exception {
        Habit savedHabit = addHabitAndReturn();
        mockMvc.perform(get(BASE_URL, testUser.getId(), savedHabit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Exercise"))
                .andExpect(jsonPath("$[0].description").value("Daily workout"));
    }

    @Test
    void testDeleteHabit() throws Exception {
        Habit savedHabit = addHabitAndReturn();
        mockMvc.perform(delete(BASE_URL + "/{habitId}", testUser.getId(), savedHabit.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(BASE_URL, testUser.getId(), savedHabit.getId()))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$", hasSize(0))));
    }

    @Test
    void testUpdateHabit() throws Exception {
        Habit existingHabit = addHabitAndReturn();
        HabitRequest habitRequest = HabitRequest.builder().name("Updated Name").description("Updated Description").build();
        mockMvc.perform(put(BASE_URL + "/{habitId}", testUser.getId(), existingHabit.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(habitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }
}
