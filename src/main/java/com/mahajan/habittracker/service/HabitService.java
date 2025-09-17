package com.mahajan.habittracker.service;

import com.mahajan.habittracker.exceptions.HabitNotFoundException;
import com.mahajan.habittracker.model.Habit;
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

    public List<Habit> getAllHabits() {
        return habitRepository.findAll();
    }

    public Habit getHabitById(Long id) {
        return habitRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Habit with id={} not found", id);
                    return new HabitNotFoundException(id);
                });
    }

    public Habit createHabit(Habit habit) {
        return habitRepository.save(habit);
    }

    public Habit updateHabit(Long id, Habit inHabit) {
        Habit outHabit = getHabitById(id);
        outHabit.setName(inHabit.getName());
        outHabit.setDescription(inHabit.getDescription());
        return habitRepository.save(outHabit);
    }

    public void deleteHabit(Long id) {
        Habit existing = getHabitById(id);
        habitRepository.delete(existing);
    }

}

