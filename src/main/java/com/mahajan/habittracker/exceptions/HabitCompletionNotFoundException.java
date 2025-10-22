package com.mahajan.habittracker.exceptions;

public class HabitCompletionNotFoundException extends RuntimeException {
    public HabitCompletionNotFoundException(Long habitId, String date) {
        super("No completion found for habit with ID " + habitId + " on " + date);
    }
}
