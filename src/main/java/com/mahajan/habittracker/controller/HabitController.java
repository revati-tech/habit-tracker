package com.mahajan.habittracker.controller;

import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.repository.HabitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    @Autowired
    private HabitRepository habitRepository;

    @GetMapping
    public List<Habit> findAll() {
        return habitRepository.findAll();
    }

    @PostMapping
    public Habit save(@RequestBody Habit habit) {
        return habitRepository.save(habit);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        habitRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
