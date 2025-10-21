package com.mahajan.habittracker.controller;

import com.mahajan.habittracker.dto.UserResponse;
import com.mahajan.habittracker.model.User;
import com.mahajan.habittracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * Get details of the currently authenticated user.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal(expression = "username") String email) {
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(UserResponse.from(user));
    }
}
