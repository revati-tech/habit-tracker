package com.mahajan.habittracker.mapper;

import com.mahajan.habittracker.dto.UserRequest;
import com.mahajan.habittracker.dto.UserResponse;
import com.mahajan.habittracker.model.User;

public class UserMapper {
    public static User toUser(UserRequest dto) {
        return User.builder()
                .email(dto.getEmail())
                .build();
    }

    public static UserResponse toResponse(User user) {
        return UserResponse.builder().id(user.getId()).email(user.getEmail()).build();
    }
}
