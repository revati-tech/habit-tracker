package com.mahajan.habittracker.exceptions;

public class HabitNotFoundException extends RuntimeException {
    public HabitNotFoundException(Long habitId, String userEmail) {
        super(String.format("Habit with id=%s not found for user with email=%s",
                habitId, userEmail));
    }
}
