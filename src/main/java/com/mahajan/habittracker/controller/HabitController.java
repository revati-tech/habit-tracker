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
    public List<HabitResponse> getAllHabits() {
        return habitService.getHabitsForUser(getCurrentUser())
                .stream().map(h -> HabitResponse.builder()
                        .id(h.getId()).name(h.getName()).description(h.getDescription()).build())
                .toList();
    }

    @PostMapping
    public HabitResponse createHabit(@Valid @RequestBody() HabitRequest habitRequest) {
        Habit habit = Habit.builder().name(habitRequest
                .getName()).description(habitRequest.getDescription()).build();
        habitService.createHabitForUser(habit, getCurrentUser());
        return HabitResponse.builder()
                .id(habit.getId()).name(habit.getName()).description(habit.getDescription()).build();
    }

    @DeleteMapping("/{habitId}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long habitId) {
        habitService.deleteHabitForUser(habitId, getCurrentUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{habitId}")
    public HabitResponse getHabitById(@PathVariable Long habitId) {
        Habit habit = habitService.getHabitByIdForUser(habitId, getCurrentUser());
        return HabitResponse.builder()
                .id(habit.getId()).name(habit.getName()).description(habit.getDescription()).build();
    }

    @PutMapping("/{habitId}")
    public HabitResponse updateHabit(@PathVariable Long habitId, @Valid @RequestBody() HabitRequest habitRequest) {
        Habit habit = Habit.builder().id(habitId).name(habitRequest.getName())
                .description(habitRequest.getDescription()).build();
        Habit updated = habitService.updateHabitForUser(habit, getCurrentUser());
        return HabitResponse.builder().id(updated.getId()).name(updated.getName()).description(updated.getDescription()).build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.getUserByEmail(email);
    }
}