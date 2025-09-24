package com.mahajan.habittracker.exceptions;

import com.mahajan.habittracker.model.HabitKey;

public class HabitNotFoundException extends RuntimeException {
    public HabitNotFoundException(HabitKey key) {
        super(String.format("Habit with id=%s not found for user with id=%s",
                key.getHabitId(), key.getUserId()));
    }
}
