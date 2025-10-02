package com.mahajan.habittracker.repository;

import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {
    List<Habit> findByUser(User user);
    Optional<Habit> findByIdAndUser(Long id, User user);
}