package com.mahajan.habittracker.controller;

import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.repository.HabitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    @Autowired
    private HabitRepository habitRepository;

    @GetMapping
    public List<Habit> getAllHabits() {
        return habitRepository.findAll();
    }

    @PostMapping
    public Habit createHabit(@RequestBody Habit habit) {
        return habitRepository.save(habit);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long id) {
        habitRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habit> getHabitById(@PathVariable Long id) {
       return ResponseEntity.ok(findHabit(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Habit> updateHabit(@PathVariable Long id, @RequestBody Habit habitRequest) {
        Habit updatedHabit = findHabit(id);
        updatedHabit.setName(habitRequest.getName());
        updatedHabit.setDescription(habitRequest.getDescription());
        return ResponseEntity.ok(habitRepository.save(updatedHabit));
    }

    private Habit findHabit(Long id) {
        return habitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Habit Not Found"));
    }
}