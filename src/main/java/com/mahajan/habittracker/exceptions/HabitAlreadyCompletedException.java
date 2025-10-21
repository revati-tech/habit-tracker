package com.mahajan.habittracker.exceptions;

public class HabitAlreadyCompletedException extends RuntimeException {
    public HabitAlreadyCompletedException(Long habitId, String date) {
        super("Habit with ID " + habitId + " is already completed for " + date);
    }
}
