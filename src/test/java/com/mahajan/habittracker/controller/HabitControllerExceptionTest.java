package com.mahajan.habittracker.controller;

import com.mahajan.habittracker.config.SecurityTestConfig;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")@Import(SecurityTestConfig.class)
class HabitControllerExceptionTest {
    private static final String BASE_URL = "/api/users/{userId}/habits/{habitId}";

    private static final Long TEST_USER_ID = 100L;

    private static final Long TEST_HABIT_ID = 42L;

    private static final HabitKey TEST_HABIT_KEY =
            HabitKey.of(TEST_USER_ID, TEST_HABIT_ID);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HabitService habitService;

    @Test
    void testGetHabitByIdHabitNotFound() throws Exception {
        when(habitService.getHabitByIdForUser(TEST_HABIT_KEY))
                .thenThrow(new HabitNotFoundException(TEST_HABIT_KEY));

        ResultActions result = mockMvc.perform(get(BASE_URL, TEST_USER_ID, TEST_HABIT_ID)
                .contentType(MediaType.APPLICATION_JSON));
        assertHabitNotFound(result);
    }

    @Test
    void testUpdateHabitNotFound() throws Exception {
        when(habitService.updateHabitForUser(any(HabitKey.class), any(Habit.class)))
                .thenThrow(new HabitNotFoundException(TEST_HABIT_KEY));

        String body = "{\"name\":\"New Name\",\"description\":\"New Description\"}";

        ResultActions result = mockMvc.perform(put(BASE_URL, TEST_USER_ID, TEST_HABIT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
        assertHabitNotFound(result);
    }

    @Test
    void testGetHabitByIdUserNotFound() throws Exception {
        when(habitService.getHabitByIdForUser(TEST_HABIT_KEY))
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
        doThrow(new HabitNotFoundException(TEST_HABIT_KEY))
                .when(habitService).deleteHabitForUser(TEST_HABIT_KEY);

        ResultActions result = mockMvc.perform(delete(BASE_URL, TEST_USER_ID, TEST_HABIT_ID)
                .contentType(MediaType.APPLICATION_JSON));
        assertHabitNotFound(result);
    }


    @Test
    void testUpdateHabitMissingBody() throws Exception {
        // No need to stub service, request will fail before service is called
        mockMvc.perform(put(BASE_URL, TEST_USER_ID, TEST_HABIT_ID)
                        .contentType(MediaType.APPLICATION_JSON)) // body is empty
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    private void assertHabitNotFound(ResultActions result) throws Exception {
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(String.format("Habit with id=%s not found for user with id=%s", TEST_HABIT_ID, TEST_USER_ID)))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}