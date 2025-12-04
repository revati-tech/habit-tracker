package com.mahajan.habittracker.service;

import com.mahajan.habittracker.dto.StreakResult;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.model.HabitCompletion;
import com.mahajan.habittracker.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StreakCalculationServiceTest {

    private StreakCalculationService streakCalculationService;
    private Habit habit;
    private User user;

    @BeforeEach
    void setUp() {
        streakCalculationService = new StreakCalculationService();
        habit = Habit.builder().id(1L).name("Test Habit").build();
        user = User.builder().id(1L).email("test@example.com").build();
    }

    @Test
    @DisplayName("Should return 0 for both streaks when no completions exist")
    void testNoCompletions() {
        List<HabitCompletion> completions = new ArrayList<>();
        StreakResult result = streakCalculationService.calculateStreaks(completions);

        assertThat(result.currentStreak()).isEqualTo(0);
        assertThat(result.longestStreak()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return 0 for both streaks when completions is null")
    void testNullCompletions() {
        StreakResult result = streakCalculationService.calculateStreaks(null);

        assertThat(result.currentStreak()).isEqualTo(0);
        assertThat(result.longestStreak()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should calculate current streak when today is completed")
    void testCurrentStreakWithTodayCompleted() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);

        List<HabitCompletion> completions = List.of(
                createCompletion(today),
                createCompletion(yesterday),
                createCompletion(twoDaysAgo)
        );

        StreakResult result = streakCalculationService.calculateStreaks(completions);

        assertThat(result.currentStreak()).isEqualTo(3);
        assertThat(result.longestStreak()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should calculate current streak when yesterday is completed but not today")
    void testCurrentStreakWithYesterdayCompleted() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);
        LocalDate threeDaysAgo = today.minusDays(3);

        List<HabitCompletion> completions = List.of(
                createCompletion(yesterday),
                createCompletion(twoDaysAgo),
                createCompletion(threeDaysAgo)
        );

        StreakResult result = streakCalculationService.calculateStreaks(completions);

        assertThat(result.currentStreak()).isEqualTo(3);
        assertThat(result.longestStreak()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should return 0 current streak when most recent completion is before yesterday")
    void testCurrentStreakBroken() {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(3);
        LocalDate fourDaysAgo = today.minusDays(4);

        List<HabitCompletion> completions = List.of(
                createCompletion(threeDaysAgo),
                createCompletion(fourDaysAgo)
        );

        StreakResult result = streakCalculationService.calculateStreaks(completions);

        assertThat(result.currentStreak()).isEqualTo(0);
        assertThat(result.longestStreak()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find longest streak even when current streak is broken")
    void testLongestStreakWithGaps() {
        LocalDate today = LocalDate.now();
        LocalDate tenDaysAgo = today.minusDays(10);
        LocalDate nineDaysAgo = today.minusDays(9);
        LocalDate eightDaysAgo = today.minusDays(8);
        LocalDate sevenDaysAgo = today.minusDays(7);
        LocalDate fiveDaysAgo = today.minusDays(5);
        LocalDate fourDaysAgo = today.minusDays(4);

        List<HabitCompletion> completions = List.of(
                createCompletion(tenDaysAgo),
                createCompletion(nineDaysAgo),
                createCompletion(eightDaysAgo),
                createCompletion(sevenDaysAgo),
                createCompletion(fiveDaysAgo),
                createCompletion(fourDaysAgo)
        );

        StreakResult result = streakCalculationService.calculateStreaks(completions);

        assertThat(result.currentStreak()).isEqualTo(0); // Broken (most recent is 4 days ago)
        assertThat(result.longestStreak()).isEqualTo(4); // 10, 9, 8, 7 days ago
    }

    @Test
    @DisplayName("Should handle multiple completions on the same date (count as one day)")
    void testMultipleCompletionsSameDate() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // Create multiple completions on the same dates
        List<HabitCompletion> completions = List.of(
                createCompletion(today),
                createCompletion(today), // Duplicate
                createCompletion(yesterday),
                createCompletion(yesterday) // Duplicate
        );

        StreakResult result = streakCalculationService.calculateStreaks(completions);

        assertThat(result.currentStreak()).isEqualTo(2); // Should count as 2 days, not 4
        assertThat(result.longestStreak()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle non-consecutive dates correctly")
    void testNonConsecutiveDates() {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(3);
        LocalDate fiveDaysAgo = today.minusDays(5);
        LocalDate sixDaysAgo = today.minusDays(6);

        List<HabitCompletion> completions = List.of(
                createCompletion(threeDaysAgo),
                createCompletion(fiveDaysAgo),
                createCompletion(sixDaysAgo)
        );

        StreakResult result = streakCalculationService.calculateStreaks(completions);

        assertThat(result.currentStreak()).isEqualTo(0); // Broken
        assertThat(result.longestStreak()).isEqualTo(2); // 6 and 5 days ago
    }

    @Test
    @DisplayName("Should handle single completion correctly")
    void testSingleCompletion() {
        LocalDate today = LocalDate.now();

        List<HabitCompletion> completions = List.of(createCompletion(today));

        StreakResult result = streakCalculationService.calculateStreaks(completions);

        assertThat(result.currentStreak()).isEqualTo(1);
        assertThat(result.longestStreak()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle single completion from yesterday")
    void testSingleCompletionYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<HabitCompletion> completions = List.of(createCompletion(yesterday));

        StreakResult result = streakCalculationService.calculateStreaks(completions);

        assertThat(result.currentStreak()).isEqualTo(1);
        assertThat(result.longestStreak()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find longest streak in middle of completion history")
    void testLongestStreakInMiddle() {
        LocalDate today = LocalDate.now();
        LocalDate tenDaysAgo = today.minusDays(10);
        LocalDate nineDaysAgo = today.minusDays(9);
        LocalDate eightDaysAgo = today.minusDays(8);
        LocalDate fiveDaysAgo = today.minusDays(5);
        LocalDate fourDaysAgo = today.minusDays(4);
        LocalDate threeDaysAgo = today.minusDays(3);
        LocalDate yesterday = today.minusDays(1);

        List<HabitCompletion> completions = List.of(
                createCompletion(tenDaysAgo),
                createCompletion(nineDaysAgo),
                createCompletion(eightDaysAgo),
                createCompletion(fiveDaysAgo),
                createCompletion(fourDaysAgo),
                createCompletion(threeDaysAgo),
                createCompletion(yesterday)
        );

        StreakResult result = streakCalculationService.calculateStreaks(completions);

        // Current streak: starts from yesterday, but 2 days ago is missing, so streak = 1
        assertThat(result.currentStreak()).isEqualTo(1);
        // Longest streak: either 10-9-8 (3 days) or 5-4-3 (3 days)
        assertThat(result.longestStreak()).isEqualTo(3);
    }

    private HabitCompletion createCompletion(LocalDate date) {
        return HabitCompletion.builder()
                .habit(habit)
                .user(user)
                .completionDate(date)
                .build();
    }
}

