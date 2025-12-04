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
@RequiredArgsConstructor
public class HabitCompletionController {

    private final HabitCompletionService completionService;
    private final HabitService habitService;
    private final UserService userService;

    /**
     * Returns all habit completions for the given date (or today if not provided) for the authenticated user.
     */
    @GetMapping("/api/habits/completions")
    public ResponseEntity<List<HabitCompletionResponse>> getCompletionsByDate(
            @RequestParam(required = false) String date,
            @AuthenticationPrincipal(expression = "username") String email) {

        User currentUser = userService.getUserByEmail(email);

        LocalDate completionDate = (date != null)
                ? LocalDate.parse(date)
                : LocalDate.now();

        var responses = completionService.getCompletionsByDate(currentUser, completionDate)
                .stream()
                .map(HabitCompletionResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Marks a habit as completed for the given date (or today if not provided).
     */
    @PostMapping("/api/habits/{habitId}/completions")
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
     * Unmarks a habit as completed for the given date
     */
    @DeleteMapping("/api/habits/{habitId}/completions/{date}")
    public ResponseEntity<Void> unmarkCompleted(
            @PathVariable Long habitId,
            @PathVariable String date,
            @AuthenticationPrincipal(expression = "username") String email) {

        User currentUser = userService.getUserByEmail(email);
        Habit habit = habitService.getHabitByIdForUser(habitId, currentUser);

        completionService.unmarkCompleted(habit, currentUser, LocalDate.parse(date));

        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * Returns all completion dates for the given habit and authenticated user.
     */
    @GetMapping("/api/habits/{habitId}/completions")
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
