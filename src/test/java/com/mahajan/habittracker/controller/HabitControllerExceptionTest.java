package com.mahajan.habittracker.controller;

import com.mahajan.habittracker.exceptions.HabitNotFoundException;
import com.mahajan.habittracker.exceptions.UserNotFoundException;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.model.HabitKey;
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
    private final static String BASE_URL = "/api/users/{userId}/habits/{habitId}";

    private final static Long TEST_USER_ID = 100L;

    private final static Long TEST_HABIT_ID = 42L;

    private final static HabitKey TEST_HABIT_KEY =
            HabitKey.of(TEST_USER_ID, TEST_HABIT_ID);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HabitService habitService;

    @Test
    void testGetHabitByIdHabitNotFound() throws Exception {
        when(habitService.getHabitForUserById(HabitKey.of(TEST_USER_ID, TEST_HABIT_ID)))
                .thenThrow(new HabitNotFoundException(TEST_HABIT_ID));

        ResultActions result = mockMvc.perform(get(BASE_URL, TEST_USER_ID, TEST_HABIT_ID)
                .contentType(MediaType.APPLICATION_JSON));
        assertHabitNotFound(result, TEST_HABIT_ID);
    }

    @Test
    void testUpdateHabitNotFound() throws Exception {
        when(habitService.updateHabit(any(HabitKey.class), any(Habit.class)))
                .thenThrow(new HabitNotFoundException(TEST_HABIT_ID));

        String body = "{\"name\":\"New Name\",\"description\":\"New Description\"}";

        ResultActions result = mockMvc.perform(put(BASE_URL, TEST_USER_ID, TEST_HABIT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body));
        assertHabitNotFound(result, TEST_HABIT_ID);
    }

    @Test
    void testGetHabitByIdUserNotFound() throws Exception {
        when(habitService.getHabitForUserById(HabitKey.of(TEST_USER_ID, TEST_HABIT_ID)))
                .thenThrow(new UserNotFoundException(TEST_USER_ID));

        ResultActions result = mockMvc.perform(get(BASE_URL, TEST_USER_ID, TEST_HABIT_ID)
                .contentType(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User with id " + TEST_USER_ID + " not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testDeleteHabitNotFound() throws Exception {
        doThrow(new HabitNotFoundException(TEST_HABIT_ID))
                .when(habitService).deleteHabit(TEST_HABIT_KEY);

        ResultActions result = mockMvc.perform(delete(BASE_URL, TEST_USER_ID, TEST_HABIT_ID)
                .contentType(MediaType.APPLICATION_JSON));
        assertHabitNotFound(result, TEST_HABIT_ID);
    }



    @Test
    void testUpdateHabitMissingBody() throws Exception {
        // No need to stub service, request will fail before service is called
        mockMvc.perform(put(BASE_URL, TEST_USER_ID, TEST_HABIT_ID)
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