package com.mahajan.habittracker.repository;

import com.mahajan.habittracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    default Optional<User> findByEmail(String email) {
        return Optional.empty();
    }
}
