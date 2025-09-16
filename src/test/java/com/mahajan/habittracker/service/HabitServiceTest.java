package com.mahajan.habittracker.service;

import com.mahajan.habittracker.exceptions.HabitNotFoundException;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.repository.HabitRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
@Transactional
class HabitServiceTest {

    @Mock
    private HabitRepository habitRepository;

    @InjectMocks
    private HabitService habitService;

    @Test
    void testGetAllHabits() {
        List<Habit> habits = Arrays.asList(
                new Habit("Exercise", "Morning workout"),
                new Habit("Meditation", "Daily 10 min meditation")
        );

        when(habitRepository.findAll()).thenReturn(habits);

        List<Habit> result = habitService.getAllHabits();

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("Exercise", result.get(0).getName());
    }

    @Test
    void testGetHabitById() {
        Habit habit = new Habit("Exercise", "Morning workout");
        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));

        Habit result = habitService.getHabitById(1L);

        Assertions.assertEquals("Exercise", result.getName());
    }

    @Test
    void testGetHabitByIdNonExistent() {
        when(habitRepository.findById(99L)).thenReturn(Optional.empty());

        Assertions.assertThrows(HabitNotFoundException.class, () ->
                habitService.getHabitById(99L));
    }

    @Test
    void testCreateHabit() {
        Habit habit = new Habit("Reading", "Read 20 pages");
        Habit savedHabit = new Habit(1L, "Reading", "Read 20 pages");

        when(habitRepository.save(any(Habit.class))).thenReturn(savedHabit);

        Habit result = habitService.createHabit(habit);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1L, result.getId());
        Assertions.assertEquals("Reading", result.getName());
        Assertions.assertEquals("Read 20 pages", result.getDescription());

        verify(habitRepository, times(1)).save(habit);
    }

    @Test
    void testUpdateHabit() {
        Habit existing = new Habit("Old Name", "Old description");
        Habit updates = new Habit("New Name", "New description");

        when(habitRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(habitRepository.save(any(Habit.class))).thenReturn(existing);

        Habit result = habitService.updateHabit(1L, updates);

        Assertions.assertEquals("New Name", result.getName());
        Assertions.assertEquals("New description", result.getDescription());
    }

    @Test
    void testUpdateHabitNonExistent() {
        Habit updates = new Habit("New Name", "New description");
        when(habitRepository.findById(99L)).thenReturn(Optional.empty());

        Assertions.assertThrows(HabitNotFoundException.class, () ->
                habitService.updateHabit(99L, updates));
    }

    @Test
    void testDeleteHabit() {
        Habit existing = new Habit("Exercise", "Workout");

        when(habitRepository.findById(1L)).thenReturn(Optional.of(existing));
        doNothing().when(habitRepository).delete(existing);

        habitService.deleteHabit(1L);

        verify(habitRepository, times(1)).delete(existing);
    }

    @Test
    void testDeleteHabitNonExistent() {
        when(habitRepository.findById(99L)).thenReturn(Optional.empty());

        Assertions.assertThrows(HabitNotFoundException.class, () ->
                habitService.deleteHabit(99L));
    }
}
