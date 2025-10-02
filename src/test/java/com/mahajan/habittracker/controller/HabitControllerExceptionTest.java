package com.mahajan.habittracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahajan.habittracker.exceptions.HabitNotFoundException;
import com.mahajan.habittracker.exceptions.UserNotFoundException;
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
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HabitController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class HabitControllerExceptionTest {
    private static final String BASE_URL = "/api/habits";

    private static final String BASE_URL_WITH_ID = "/api/habits/{habitId}";

    private static final Long TEST_HABIT_ID = 100L;

    private static final Long TEST_USER_ID = 1L;

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
        testHabit = Habit.builder().id(TEST_HABIT_ID).name("Exercise").description("Daily workout").build();

        // Set up a fake authenticated user
        testUser = User.builder().id(TEST_USER_ID).email("test@example.com").build();
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(testUser.getEmail(), null));

        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
    }

    @Test
    void testGetHabitByIdHabitNotFound() throws Exception {
        when(habitService.getHabitByIdForUser(anyLong(), any(User.class)))
                .thenThrow(new HabitNotFoundException(TEST_HABIT_ID, testUser.getEmail()));

        ResultActions result = mockMvc.perform(get(BASE_URL_WITH_ID, TEST_HABIT_ID)
                .contentType(MediaType.APPLICATION_JSON));
        assertHabitNotFound(result);
    }

    @Test
    void testDeleteHabitNotFound() throws Exception {
        doThrow(new HabitNotFoundException(TEST_HABIT_ID, testUser.getEmail()))
                .when(habitService).deleteHabitForUser(anyLong(), any(User.class));

        ResultActions result = mockMvc.perform(delete(BASE_URL_WITH_ID, TEST_HABIT_ID)
                .contentType(MediaType.APPLICATION_JSON));
        assertHabitNotFound(result);
    }

    @Test
    void testUpdateHabitMissingBody() throws Exception {
        // No need to stub service, request will fail before service is called
        mockMvc.perform(put(BASE_URL_WITH_ID, TEST_HABIT_ID)
                        .contentType(MediaType.APPLICATION_JSON)) // body is empty
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void testUpdateHabitNotFound() throws Exception {
        when(habitService.updateHabitForUser(any(Habit.class), any(User.class)))
                .thenThrow(new HabitNotFoundException(TEST_HABIT_ID, testUser.getEmail()));

        String body = "{\"name\":\"New Name\",\"description\":\"New Description\"}";

        ResultActions result = mockMvc.perform(put(BASE_URL_WITH_ID, TEST_HABIT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
        assertHabitNotFound(result);
    }

    @Test
    void testGetHabitByIdUserNotFound() throws Exception {
        when(userService.getUserByEmail(any(String.class)))
                .thenThrow(new UserNotFoundException(TEST_USER_ID));

        ResultActions result = mockMvc.perform(get(BASE_URL_WITH_ID, TEST_HABIT_ID)
                .contentType(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User with id " + TEST_USER_ID + " not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private void assertHabitNotFound(ResultActions result) throws Exception {
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(
                        String.format("Habit with id=%s not found for user with email=%s",
                                TEST_HABIT_ID, testUser.getEmail())))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}