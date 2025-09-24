package com.mahajan.habittracker.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User with id " + id + " not found");
    }

    public UserNotFoundException(String userEmail) {
        super("User with email " + userEmail + " not found");
    }
}
