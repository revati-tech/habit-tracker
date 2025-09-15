package com.mahajan.habittracker.controller;

import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.service.HabitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;

    @GetMapping
    public List<Habit> getAllHabits() {
        return habitService.getAllHabits();
    }

    @PostMapping
    public Habit createHabit(@RequestBody Habit habit) {
        return habitService.createHabit(habit);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long id) {
        habitService.deleteHabit(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habit> getHabitById(@PathVariable Long id) {
       return ResponseEntity.ok(findHabit(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Habit> updateHabit(@PathVariable Long id, @RequestBody Habit inHabit) {
        return ResponseEntity.ok(habitService.updateHabit(id, inHabit));
    }

    private Habit findHabit(Long id) {
       return habitService.getHabitById(id);
    }
}