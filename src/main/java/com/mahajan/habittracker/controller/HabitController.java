package com.mahajan.habittracker.controller;

import com.mahajan.habittracker.dto.HabitRequest;
import com.mahajan.habittracker.dto.HabitResponse;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.model.User;
import com.mahajan.habittracker.service.HabitService;
import com.mahajan.habittracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<HabitResponse>> getHabits(
            @AuthenticationPrincipal(expression = "username") String email) {
        User user = userService.getUserByEmail(email);
        List<HabitResponse> habits = habitService.getHabitsForUser(user)
                .stream().map(HabitResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(habits);
    }

    @PostMapping
    public ResponseEntity<HabitResponse> createHabit(
            @Valid @RequestBody() HabitRequest habitRequest,
            @AuthenticationPrincipal(expression = "username") String email) {
        Habit habit = habitRequest.toEntity();
        User user = userService.getUserByEmail(email);
        Habit createdHabit = habitService.createHabitForUser(habit, user);
        URI location = URI.create("/api/habits/" + createdHabit.getId());
        return ResponseEntity.created(location).body(HabitResponse.fromEntity(createdHabit));
    }

    @DeleteMapping("/{habitId}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long habitId,
                                            @AuthenticationPrincipal(expression = "username") String email) {
        User user = userService.getUserByEmail(email);
        habitService.deleteHabitForUser(habitId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{habitId}")
    public ResponseEntity<HabitResponse> getHabit(
            @PathVariable Long habitId,
            @AuthenticationPrincipal(expression = "username") String email) {
        User user = userService.getUserByEmail(email);
        Habit habit = habitService.getHabitByIdForUser(habitId, user);
        return ResponseEntity.ok(HabitResponse.fromEntity(habit));
    }

    @PutMapping("/{habitId}")
    public ResponseEntity<HabitResponse> updateHabit(
            @PathVariable Long habitId,
            @Valid @RequestBody() HabitRequest habitRequest,
            @AuthenticationPrincipal(expression = "username") String email) {
        Habit habit = habitRequest.toEntity();
        habit.setId(habitId);
        User user = userService.getUserByEmail(email);
        Habit updated = habitService.updateHabitForUser(habit, user);
        return ResponseEntity.ok(HabitResponse.fromEntity(updated));
    }
}