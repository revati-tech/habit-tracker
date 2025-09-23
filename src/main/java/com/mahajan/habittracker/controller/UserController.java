package com.mahajan.habittracker.controller;

import com.mahajan.habittracker.dto.UserRequest;
import com.mahajan.habittracker.dto.UserResponse;
import com.mahajan.habittracker.mapper.UserMapper;
import com.mahajan.habittracker.model.User;
import com.mahajan.habittracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserResponse> getUsers() {
        return userService.getALLUsers()
                .stream().map(UserMapper::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return UserMapper.toResponse(userService.getUserById(id));
    }

    @GetMapping("/search")
    public UserResponse getUserByEmail(@RequestParam String email) {
        return UserMapper.toResponse(userService.getUserByEmail(email));
    }

    @PostMapping
    public UserResponse createUser(@RequestBody UserRequest request) {
        User user = userService.createUser(UserMapper.toUser(request));
        return UserMapper.toResponse(user);
    }
}
