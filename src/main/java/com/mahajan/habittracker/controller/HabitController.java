package com.mahajan.habittracker.controller;

import com.mahajan.habittracker.dto.HabitRequest;
import com.mahajan.habittracker.dto.HabitResponse;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.service.HabitService;
import jakarta.validation.Valid;
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
    public List<HabitResponse> getAllHabits() {
        return habitService.getAllHabits()
                .stream().map(h -> HabitResponse.builder()
                        .id(h.getId()).name(h.getName()).description(h.getDescription()).build())
                .toList();
    }

    @PostMapping
    public Habit createHabit(@Valid @RequestBody(required = true) HabitRequest habitRequest) {
        Habit habit = Habit.builder().name(habitRequest
                .getName()).description(habitRequest.getDescription()).build();
        return habitService.createHabit(habit);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long id) {
        habitService.deleteHabit(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public HabitResponse getHabitById(@PathVariable Long id) {
       Habit habit = habitService.getHabitById(id);
       return HabitResponse.builder()
               .id(habit.getId()).name(habit.getName()).description(habit.getDescription()).build();
    }

    @PutMapping("/{id}")
    public HabitResponse updateHabit(@PathVariable Long id, @Valid @RequestBody(required = true) HabitRequest habitRequest) {
        Habit habit = Habit.builder().name(habitRequest.getName())
                .description(habitRequest.getDescription()).build();
        Habit updated = habitService.updateHabit(id, habit);
        return HabitResponse.builder().id(updated.getId()).name(updated.getName()).description(habit.getDescription()).build();
    }
}