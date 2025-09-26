package com.mahajan.habittracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahajan.habittracker.dto.UserRequest;
import com.mahajan.habittracker.model.User;
import com.mahajan.habittracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
})

class UserControllerTest {
    private static final String EMAIL = "test@test.com";

    private static final String PASSWORD = "password123";

    private static final String BASE_URL = "/api/users/{userId}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testCreateUser() throws Exception {
        UserRequest request = UserRequest.builder()
                .email(EMAIL).password(PASSWORD).build();

        ResultActions result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));
        // Fetch saved user from repository
        User savedUser = userRepository.findByEmail(EMAIL)
                .orElseThrow(() -> new AssertionError("User not found in DB"));

        // Assert password was encoded properly
        String encodedPassword = savedUser.getPassword();
        assertNotEquals(PASSWORD, encodedPassword); // make sure it's not plain text
        assertTrue(passwordEncoder.matches(PASSWORD, encodedPassword)); // ver

    }

    @Test
    void getAllUsers() throws Exception {
        User user1 = addUserAndReturn();
        UserRequest request2 = UserRequest.builder().email("second@test.com").password(PASSWORD).build();
        ResultActions result2 = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));
        User user2 = objectMapper.readValue(result2.andReturn().getResponse().getContentAsString(), User.class);

        ResultActions result = mockMvc.perform(get("/api/users"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.id==" + user1.getId() + ")].email").value(EMAIL))
                .andExpect(jsonPath("$[?(@.id==" + user2.getId() + ")].email").value("second@test.com"));
    }


    @Test
    void testGetUserById() throws Exception {
        User user = addUserAndReturn();
        ResultActions result = mockMvc.perform(get(BASE_URL, user.getId()));
        assertUserResponse(result, user);
    }

    private User addUserAndReturn() throws Exception {
        UserRequest request = UserRequest.builder().email(EMAIL).password(PASSWORD).build();

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
