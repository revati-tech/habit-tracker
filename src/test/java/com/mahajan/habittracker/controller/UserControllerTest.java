package com.mahajan.habittracker.controller;

import com.mahajan.habittracker.exceptions.UserNotFoundException;
import com.mahajan.habittracker.model.User;
import com.mahajan.habittracker.security.JwtAuthFilter;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link UserController}.
 */
@WebMvcTest(controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("GET /api/users/me should return the authenticated user's details")
    @WithMockUser(username = "test@example.com")
    void testCurrentUser() throws Exception {
        // Arrange
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        Mockito.when(userService.getUserByEmail("test@example.com"))
                .thenReturn(mockUser);

        // Act & Assert
        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("test@example.com")));

        Mockito.verify(userService).getUserByEmail("test@example.com");
    }

    // ❌ Negative Case 1: User Not Found
    @Test
    @DisplayName("GET /api/users/me returns 404 when user not found")
    @WithMockUser(username = "missing@example.com")
    void testCurrentUserNotFound() throws Exception {
        Mockito.when(userService.getUserByEmail("missing@example.com"))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(userService).getUserByEmail("missing@example.com");
    }
}
