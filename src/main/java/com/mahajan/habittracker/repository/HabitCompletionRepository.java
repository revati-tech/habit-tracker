package com.mahajan.habittracker.repository;

import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.model.HabitCompletion;
import com.mahajan.habittracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Long> {
    boolean existsByHabitAndUserAndCompletionDate(Habit habit, User user, LocalDate date);

    List<HabitCompletion> findAllByHabitAndUserOrderByCompletionDateDesc(Habit habit, User user);

    Optional<HabitCompletion> findByHabitAndUserAndCompletionDate(Habit habit, User user, LocalDate date);
}
