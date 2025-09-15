package com.mahajan.habittracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahajan.habittracker.model.Habit;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class HabitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Habit habit;

    @BeforeEach
    void setUp() {
        habit = new Habit();
        habit.setName("Exercise");
        habit.setDescription("Daily workout");
    }

    private Habit addHabitAndReturn() throws Exception {
        String response = mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(habit)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, Habit.class);
    }

    @Test
    void testCreateHabit() throws Exception {
        mockMvc.perform(post("/api/habits").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(habit))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Exercise"))
                .andExpect(jsonPath("$.description").value("Daily workout"));
    }

    @Test
    void testGetAllHabits() throws Exception {
       addHabitAndReturn();
        mockMvc.perform(get("/api/habits"))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$", hasSize(1))))
                .andExpect(jsonPath("$[0].name").value("Exercise"))
                .andExpect(jsonPath("$[0].description").value("Daily workout"));
    }

    @Test
    void testGetHabitById() throws Exception {
        Habit savedHabit = addHabitAndReturn();
        mockMvc.perform(get("/api/habits/{id}", savedHabit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Exercise"))
                .andExpect(jsonPath("$.description").value("Daily workout"));
    }

    @Test
    void testDeleteHabit() throws Exception {
        Habit savedHabit = addHabitAndReturn();
        mockMvc.perform(delete("/api/habits/{id}", savedHabit.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/habits"))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$", hasSize(0))));
    }

    @Test
    void testUpdateHabit() throws Exception {
        Habit habitRequest = addHabitAndReturn();
        habitRequest.setName("Updated Name");
        habitRequest.setDescription("Updated Description");
       mockMvc.perform(put("/api/habits/{id}",  habitRequest.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(habitRequest)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Updated Name"))
               .andExpect(jsonPath("$.description").value("Updated Description"));
    }
}
