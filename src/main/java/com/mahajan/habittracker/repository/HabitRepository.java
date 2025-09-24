package com.mahajan.habittracker.repository;

import com.mahajan.habittracker.model.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {
    Optional<Habit> findByIdAndUserId(Long id, Long userId);

    List<Habit> findAllByUserId(Long userId);
}