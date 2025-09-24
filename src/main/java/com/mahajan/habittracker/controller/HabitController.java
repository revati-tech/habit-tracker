package com.mahajan.habittracker.controller;

import com.mahajan.habittracker.dto.HabitRequest;
import com.mahajan.habittracker.dto.HabitResponse;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.model.HabitKey;
import com.mahajan.habittracker.service.HabitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/habits")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;

    @GetMapping
    public List<HabitResponse> getHabitsByUser(@PathVariable Long userId) {
        return habitService.getHabitsByUser(userId)
                .stream().map(h -> HabitResponse.builder()
                        .id(h.getId()).name(h.getName()).description(h.getDescription()).build())
                .toList();
    }

    @PostMapping
    public HabitResponse createHabit(@PathVariable Long userId, @Valid @RequestBody(required = true) HabitRequest habitRequest) {
        Habit habit = Habit.builder().name(habitRequest
                .getName()).description(habitRequest.getDescription()).build();
        habitService.createHabit(userId, habit);
        return HabitResponse.builder()
                .id(habit.getId()).name(habit.getName()).description(habit.getDescription()).build();
    }

    @DeleteMapping("/{habitId}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long userId, @PathVariable Long habitId) {
        habitService.deleteHabit(HabitKey.of(userId, habitId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{habitId}")
    public HabitResponse getHabitById(@PathVariable Long userId, @PathVariable Long habitId) {
        Habit habit = habitService.getHabitForUserById(HabitKey.of(userId, habitId));
        return HabitResponse.builder()
                .id(habit.getId()).name(habit.getName()).description(habit.getDescription()).build();
    }

    @PutMapping("/{habitId}")
    public HabitResponse updateHabit(@PathVariable Long userId, @PathVariable Long habitId, @Valid @RequestBody(required = true) HabitRequest habitRequest) {
        Habit habit = Habit.builder().name(habitRequest.getName())
                .description(habitRequest.getDescription()).build();
        Habit updated = habitService.updateHabit(HabitKey.of(userId, habitId), habit);
        return HabitResponse.builder().id(updated.getId()).name(updated.getName()).description(habit.getDescription()).build();
    }
}