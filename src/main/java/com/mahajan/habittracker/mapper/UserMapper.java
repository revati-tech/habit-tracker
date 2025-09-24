package com.mahajan.habittracker.mapper;

import com.mahajan.habittracker.dto.UserRequest;
import com.mahajan.habittracker.dto.UserResponse;
import com.mahajan.habittracker.model.User;
import lombok.experimental.UtilityClass;

@UtilityClass
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
