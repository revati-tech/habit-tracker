package com.mahajan.habittracker.service;

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
}

