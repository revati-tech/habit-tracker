package com.mahajan.habittracker.controller;

import com.mahajan.habittracker.dto.HabitCompletionResponse;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.model.User;
import com.mahajan.habittracker.service.HabitCompletionService;
import com.mahajan.habittracker.service.HabitService;
import com.mahajan.habittracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/habits/{habitId}/completions")
@RequiredArgsConstructor
public class HabitCompletionController {

    private final HabitCompletionService completionService;
    private final HabitService habitService;
    private final UserService userService;

    /**
     * Marks a habit as completed for the given date (or today if not provided).
     */
    @PostMapping
    public ResponseEntity<String> markCompleted(
            @PathVariable Long habitId,
            @RequestParam(required = false) String date,
            @AuthenticationPrincipal(expression = "username") String email) {

        User currentUser = userService.getUserByEmail(email);
        Habit habit = habitService.getHabitByIdForUser(habitId, currentUser);

        LocalDate completionDate = (date != null)
                ? LocalDate.parse(date)
                : LocalDate.now();

        completionService.markCompleted(habit, currentUser, completionDate);
        return ResponseEntity.ok("Habit marked as completed for " + completionDate);
    }

    /**
     * Returns all completion dates for the given habit and authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<HabitCompletionResponse>> getCompletions(
            @PathVariable Long habitId,
            @AuthenticationPrincipal(expression = "username") String email) {

        User currentUser = userService.getUserByEmail(email);
        Habit habit = habitService.getHabitByIdForUser(habitId, currentUser);

        var responses = completionService.getCompletions(habit, currentUser)
                .stream()
                .map(HabitCompletionResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(responses);
    }

}
