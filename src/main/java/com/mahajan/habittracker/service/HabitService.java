package com.mahajan.habittracker.service;

import com.mahajan.habittracker.exceptions.HabitNotFoundException;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.model.HabitKey;
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
    private final UserService userService;

    public List<Habit> getHabitsByUser(Long userId) {
        return habitRepository.findAll();
    }

    public Habit getHabitForUserById(HabitKey key) {
        Long userId = key.getUserId();
        Long habitId = key.getHabitId();
        User user = userService.getUserById(key.getUserId());
        return habitRepository.findByIdAndUserId(key.getHabitId(), user.getId())
                .orElseThrow(() -> {
                    log.warn("Habit with id={} not found for userId={}",
                            habitId, userId);
                    return new HabitNotFoundException(key);
                });
    }

    public Habit createHabit(Long userId, Habit habit) {
        habit.setUser(userService.getUserById(userId));
        return habitRepository.save(habit);
    }

    public Habit updateHabit(HabitKey key, Habit inHabit) {
        Habit outHabit = getHabitForUserById(key);
        outHabit.setName(inHabit.getName());
        outHabit.setDescription(inHabit.getDescription());
        return habitRepository.save(outHabit);
    }

    public void deleteHabit(HabitKey key) {
        Habit existing = getHabitForUserById(key);
        habitRepository.delete(existing);
    }
}

