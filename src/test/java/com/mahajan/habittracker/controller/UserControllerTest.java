package com.mahajan.habittracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahajan.habittracker.dto.UserRequest;
import com.mahajan.habittracker.model.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {
    private static final String EMAIL = "test@test.com";

    private static final String BASE_URL = "/api/users/{userId}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateUser() throws Exception {
        UserRequest request = UserRequest.builder().email(EMAIL).build();

        ResultActions result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));

    }

    @Test
    void testGetAllUsers() throws Exception {
        User user = addUserAndReturn();
        ResultActions result = mockMvc.perform(get(BASE_URL, user.getId()));
        assertUserResponse(result, user);
    }

    @Test
    void testGetUserById() throws Exception {
        User user = addUserAndReturn();
        ResultActions result = mockMvc.perform(get(BASE_URL, user.getId()));
        assertUserResponse(result, user);
    }

    private User addUserAndReturn() throws Exception {
        UserRequest request = UserRequest.builder().email(EMAIL).build();

        ResultActions result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        return objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), User.class);
    }

    private void assertUserResponse(org.springframework.test.web.servlet.ResultActions resultActions, User user) throws Exception {
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value(EMAIL));
    }
}
