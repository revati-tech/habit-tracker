package com.mahajan.habittracker.service;

import com.mahajan.habittracker.exceptions.HabitNotFoundException;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.repository.HabitRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

    private Habit habit;

    @BeforeEach
    void setUp() {
        habit = Habit.builder().name("Exercise").description("Daily workout").build();
    }

    @Test
    void testGetAllHabits() {
        List<Habit> habits = Arrays.asList(
                habit,
                Habit.builder().name("Meditation").description("Daily 10 min meditation")
                        .build());
        when(habitRepository.findAll()).thenReturn(habits);

        List<Habit> result = habitService.getAllHabits();

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(habits.get(0).getName(), result.get(0).getName());
        Assertions.assertEquals(habits.get(0).getDescription(), result.get(0).getDescription());
        Assertions.assertEquals(habits.get(1).getName(), result.get(1).getName());
        Assertions.assertEquals(habits.get(1).getDescription(), result.get(1).getDescription());
    }

    @Test
    void testGetHabitById() {
        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));

        Habit result = habitService.getHabitById(1L);

        Assertions.assertEquals(habit.getName(), result.getName());
        Assertions.assertEquals(habit.getDescription(), result.getDescription());
    }

    @Test
    void testGetHabitByIdNonExistent() {
        when(habitRepository.findById(99L)).thenReturn(Optional.empty());

        Assertions.assertThrows(HabitNotFoundException.class, () ->
                habitService.getHabitById(99L));
    }

    @Test
    void testCreateHabit() {
        Habit savedHabit = Habit.builder().id(1L).name("Exercise").description("Daily workout").build();

        when(habitRepository.save(any(Habit.class))).thenReturn(savedHabit);

        Habit result = habitService.createHabit(habit);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1L, result.getId());
        Assertions.assertEquals(savedHabit.getName(), result.getName());
        Assertions.assertEquals(savedHabit.getDescription(), result.getDescription());

        verify(habitRepository, times(1)).save(habit);
    }

    @Test
    void testUpdateHabit() {
        Habit updates = Habit.builder().name("New Name").description("New description").build();

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(habitRepository.save(any(Habit.class))).thenReturn(habit);

        Habit result = habitService.updateHabit(1L, updates);

        Assertions.assertEquals("New Name", result.getName());
        Assertions.assertEquals("New description", result.getDescription());
    }

    @Test
    void testUpdateHabitNonExistent() {
        Habit updates = Habit.builder().name("New Name").description("New description").build();
        when(habitRepository.findById(99L)).thenReturn(Optional.empty());

        Assertions.assertThrows(HabitNotFoundException.class, () ->
                habitService.updateHabit(99L, updates));
    }

    @Test
    void testDeleteHabit() {
        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        doNothing().when(habitRepository).delete(habit);

        habitService.deleteHabit(1L);

        verify(habitRepository, times(1)).delete(habit);
    }

    @Test
    void testDeleteHabitNonExistent() {
        when(habitRepository.findById(99L)).thenReturn(Optional.empty());

        Assertions.assertThrows(HabitNotFoundException.class, () ->
                habitService.deleteHabit(99L));
    }
}
