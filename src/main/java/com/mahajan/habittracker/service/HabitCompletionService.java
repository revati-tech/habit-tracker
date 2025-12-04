package com.mahajan.habittracker.service;

import com.mahajan.habittracker.exceptions.HabitAlreadyCompletedException;
import com.mahajan.habittracker.exceptions.HabitCompletionNotFoundException;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.model.HabitCompletion;
import com.mahajan.habittracker.model.User;
import com.mahajan.habittracker.repository.HabitCompletionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HabitCompletionService {

    private final HabitCompletionRepository completionRepository;

    @Transactional
    public HabitCompletion markCompleted(Habit habit, User user, LocalDate date) {
        if (completionRepository.existsByHabitAndUserAndCompletionDate(habit, user, date)) {
            throw new HabitAlreadyCompletedException(habit.getId(), date.toString());
        }

        HabitCompletion completion = HabitCompletion.builder()
                .habit(habit)
                .user(user)
                .completionDate(date)
                .build();

        return completionRepository.save(completion);
    }

    @Transactional
    public void unmarkCompleted(Habit habit, User user, LocalDate date) {
        HabitCompletion completion = completionRepository
                .findByHabitAndUserAndCompletionDate(habit, user, date)
                .orElseThrow(() -> new HabitCompletionNotFoundException(habit.getId(), date.toString()));

        completionRepository.delete(completion);
    }

    public List<HabitCompletion> getAllCompletionsForHabit(Habit habit, User user) {
        return completionRepository.findAllByHabitAndUserOrderByCompletionDateDesc(habit, user);
    }

    public List<HabitCompletion> getCompletionsByDate(User user, LocalDate date) {
        return completionRepository.findAllByUserAndCompletionDate(user, date);
    }
}
