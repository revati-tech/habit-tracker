package com.mahajan.habittracker.controller;

import com.mahajan.habittracker.exceptions.HabitNotFoundException;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.service.HabitService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class HabitControllerExceptionTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HabitService habitService;

    @Test
    void testGetHabitByIdNotFound() throws Exception {
        Long id = 42L;
        when(habitService.getHabitById(id))
                .thenThrow(new HabitNotFoundException(id));

        ResultActions result = mockMvc.perform(get("/api/habits/{id}", id)
                .contentType(MediaType.APPLICATION_JSON));
        assertHabitNotFound(result, id);
    }

    @Test
    void testUpdateHabitNotFound() throws Exception {
        Long id = 42L;
        when(habitService.updateHabit(eq(id), any(Habit.class)))
                .thenThrow(new HabitNotFoundException(id));

        String body = "{\"name\":\"New Name\",\"description\":\"New Description\"}";

        ResultActions result = mockMvc.perform(put("/api/habits/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body));
        assertHabitNotFound(result, id);
    }

    @Test
    void testDeleteHabitNotFound() throws Exception {
        Long id = 42L;
        doThrow(new HabitNotFoundException(id))
                .when(habitService).deleteHabit(id);

        ResultActions result = mockMvc.perform(delete("/api/habits/{id}", id)
                .contentType(MediaType.APPLICATION_JSON));
        assertHabitNotFound(result, id);
    }

    @Test
    void testUpdateHabitMissingBody() throws Exception {
        Long id = 42L;

        // No need to stub service, request will fail before service is called
        mockMvc.perform(put("/api/habits/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)) // no body
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    private void assertHabitNotFound(ResultActions result, Long id) throws Exception {
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Habit with id " + id + " not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}