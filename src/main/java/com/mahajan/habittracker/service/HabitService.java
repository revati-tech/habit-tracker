package com.mahajan.habittracker.service;

import com.mahajan.habittracker.dto.StreakResult;
import com.mahajan.habittracker.exceptions.HabitNotFoundException;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.model.User;
import com.mahajan.habittracker.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HabitService {
    private final HabitRepository habitRepository;
    private final HabitCompletionService completionService;
    private final StreakCalculationService streakCalculationService;

    public List<Habit> getHabitsForUser(User user) {
        return habitRepository.findByUser(user);
    }

    public Habit getHabitByIdForUser(Long habitId, User user) {
        return habitRepository.findByIdAndUser(habitId, user)
                .orElseThrow(() -> {
                    log.warn("Habit with id={} not found for userEmail={}", habitId, user.getEmail());
                    return new HabitNotFoundException(habitId, user.getEmail());
                });
    }

    public Habit createHabitForUser(Habit habit, User user) {
        habit.setUser(user);
        return habitRepository.save(habit);
    }

    public Habit updateHabitForUser(Habit inHabit, User user) {
        Habit outHabit = getHabitByIdForUser(inHabit.getId(), user);
        outHabit.setName(inHabit.getName());
        outHabit.setDescription(inHabit.getDescription());
        return habitRepository.save(outHabit);
    }

    public void deleteHabitForUser(long inHabitId, User user) {
        Habit existing = getHabitByIdForUser(inHabitId, user);
        habitRepository.delete(existing);
    }

    /**
     * Calculates streaks for a habit based on its completions.
     *
     * @param habit The habit to calculate streaks for
     * @param user  The user who owns the habit
     * @return StreakResult containing currentStreak and longestStreak
     */
    public StreakResult calculateStreaksForHabit(Habit habit, User user) {
        List<com.mahajan.habittracker.model.HabitCompletion> completions = 
                completionService.getAllCompletionsForHabit(habit, user);
        return streakCalculationService.calculateStreaks(completions);
    }
}

