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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<HabitResponse>> getHabits() {
        List<HabitResponse> habits = habitService.getHabitsForUser(getCurrentUser())
                .stream().map(HabitResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(habits);
    }

    @PostMapping
    public ResponseEntity<HabitResponse> createHabit(@Valid @RequestBody() HabitRequest habitRequest) {
        Habit habit = HabitRequest.toEntity(habitRequest);
        habitService.createHabitForUser(habit, getCurrentUser());
        return ResponseEntity.ok(HabitResponse.fromEntity(habit));
    }

    @DeleteMapping("/{habitId}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long habitId) {
        habitService.deleteHabitForUser(habitId, getCurrentUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{habitId}")
    public ResponseEntity<HabitResponse> getHabit(@PathVariable Long habitId) {
        Habit habit = habitService.getHabitByIdForUser(habitId, getCurrentUser());
        return ResponseEntity.ok(HabitResponse.fromEntity(habit));
    }

    @PutMapping("/{habitId}")
    public ResponseEntity<HabitResponse> updateHabit(@PathVariable Long habitId, @Valid @RequestBody() HabitRequest habitRequest) {
        Habit habit = HabitRequest.toEntity(habitRequest);
        Habit updated = habitService.updateHabitForUser(habit, getCurrentUser());
        return ResponseEntity.ok(HabitResponse.fromEntity(updated));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.getUserByEmail(email);
    }
}