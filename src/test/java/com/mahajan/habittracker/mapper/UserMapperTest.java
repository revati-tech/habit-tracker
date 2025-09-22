package com.mahajan.habittracker.mapper;

import com.mahajan.habittracker.dto.UserRequest;
import com.mahajan.habittracker.dto.UserResponse;
import com.mahajan.habittracker.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {
    private static final Long ID = 1L;
    private static final String EMAIL = "test@test.com";

    @Test
    void testToUser() {
        UserRequest request = new UserRequest("test@test.com");
        User user = UserMapper.toUser(request);
        assertEquals(EMAIL, user.getEmail());
    }

    @Test
    void testToResponse() {
        User user = User.builder()
                .id(1L)
                .email(EMAIL)
                .build();

        UserResponse dto = UserMapper.toResponse(user);

        assertEquals(1L, dto.getId());
        assertEquals(EMAIL, dto.getEmail());
    }
}
