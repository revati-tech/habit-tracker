package com.mahajan.habittracker.mapper;

import com.mahajan.habittracker.dto.UserRequest;
import com.mahajan.habittracker.dto.UserResponse;
import com.mahajan.habittracker.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserMapperTest {
    private static final Long ID = 1L;
    private static final String EMAIL = "test@test.com";
    private static final String PASSWORD = "password123";

    @Test
    void testToUser() {
        UserRequest request = UserRequest.builder().email(EMAIL).password(PASSWORD).build();
        User user = UserMapper.toUser(request);
        assertEquals(EMAIL, user.getEmail());
    }

    @Test
    void testToResponse() {
        User user = User.builder()
                .id(ID)
                .email(EMAIL)
                .password(PASSWORD)
                .build();

        UserResponse dto = UserMapper.toResponse(user);

        assertEquals(ID, dto.getId());
        assertEquals(EMAIL, dto.getEmail());
    }
}
