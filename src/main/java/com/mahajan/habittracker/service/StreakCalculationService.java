package com.mahajan.habittracker.service;

import com.mahajan.habittracker.dto.StreakResult;
import com.mahajan.habittracker.model.HabitCompletion;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating habit streaks.
 * Handles current streak and longest streak calculations.
 */
@Service
public class StreakCalculationService {

    /**
     * Calculates both current and longest streaks for a habit based on its completions.
     *
     * @param completions List of habit completions
     * @return StreakResult containing currentStreak and longestStreak
     */
    public StreakResult calculateStreaks(List<HabitCompletion> completions) {
        if (completions == null || completions.isEmpty()) {
            return new StreakResult(0, 0);
        }

        // Get unique completion dates, sorted in ascending order
        // TreeSet automatically handles duplicates and maintains sorted order
        Set<LocalDate> completionDates = completions.stream()
                .map(HabitCompletion::getCompletionDate)
                .collect(Collectors.toCollection(TreeSet::new));

        // Create ArrayList for indexed iteration (needed for calculateLongestStreak)
        // and HashSet for O(1) lookup performance (needed for calculateCurrentStreak)
        // This optimization avoids O(n) contains() checks when counting backwards
        List<LocalDate> sortedDates = new ArrayList<>(completionDates);
        Set<LocalDate> dateSet = new HashSet<>(completionDates);

        int currentStreak = calculateCurrentStreak(sortedDates, dateSet);
        int longestStreak = calculateLongestStreak(sortedDates);

        return new StreakResult(currentStreak, longestStreak);
    }

    /**
     * Calculates the current streak by counting backwards from today (or yesterday if today isn't completed).
     * The streak continues as long as consecutive days are found.
     */
    private int calculateCurrentStreak(List<LocalDate> sortedDates, Set<LocalDate> dateSet) {
        if (sortedDates.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // Determine the starting date for streak calculation
        // If today is completed, start from today; otherwise start from yesterday
        LocalDate startDate;
        if (dateSet.contains(today)) {
            startDate = today;
        } else if (dateSet.contains(yesterday)) {
            startDate = yesterday;
        } else {
            // Most recent completion is before yesterday - streak is broken
            return 0;
        }

        // Count backwards from startDate
        int streak = 0;
        LocalDate currentDate = startDate;

        while (dateSet.contains(currentDate)) {
            streak++;
            currentDate = currentDate.minusDays(1);
        }

        return streak;
    }

    /**
     * Calculates the longest streak by finding the longest sequence of consecutive dates.
     */
    private int calculateLongestStreak(List<LocalDate> sortedDates) {
        if (sortedDates.isEmpty()) {
            return 0;
        }

        int longestStreak = 1;
        int currentStreak = 1;

        for (int i = 1; i < sortedDates.size(); i++) {
            LocalDate prevDate = sortedDates.get(i - 1);
            LocalDate currDate = sortedDates.get(i);

            // Check if dates are consecutive
            if (currDate.equals(prevDate.plusDays(1))) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 1; // Reset streak
            }
        }

        return longestStreak;
    }
}

