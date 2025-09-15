package com.mahajan.habittracker.exceptions;

public class HabitNotFoundException extends RuntimeException {
    public HabitNotFoundException(Long id) {
        super("Habit with id " + id + " not found");
    }
}
