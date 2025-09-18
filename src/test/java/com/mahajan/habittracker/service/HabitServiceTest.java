package com.mahajan.habittracker.service;

import com.mahajan.habittracker.exceptions.HabitNotFoundException;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.repository.HabitRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        List<Habit> habits = List.of(
                habit,
                Habit.builder().name("Meditation").description("Daily 10 min meditation")
                        .build());
        when(habitRepository.findAll()).thenReturn(habits);

        List<Habit> result = habitService.getAllHabits();

        Assertions.assertEquals(2, result.size());
        assertHabitEquals(habits.get(0), result.get(0));
        assertHabitEquals(habits.get(1), result.get(1));
    }

    @Test
    void testGetHabitById() {
        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        Habit result = habitService.getHabitById(1L);
        assertHabitEquals(habit, result);
    }

    @Test
    void testGetHabitByIdNonExistent() {
        Long id = 99L;
        when(habitRepository.findById(id)).thenReturn(Optional.empty());
        assertHabitNotFound(id, () ->   habitService.getHabitById(id));
    }

    @Test
    void testCreateHabit() {
        Habit savedHabit = Habit.builder().id(1L).name("Exercise").description("Daily workout").build();

        when(habitRepository.save(any(Habit.class))).thenReturn(savedHabit);

        Habit result = habitService.createHabit(habit);

        Assertions.assertNotNull(result);
        assertHabitEquals(savedHabit, result);

        verify(habitRepository, times(1)).save(habit);
    }

    @Test
    void testUpdateHabit() {
        Habit updates = Habit.builder().name("New Name").description("New description").build();

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(habitRepository.save(any(Habit.class))).thenReturn(habit);

        Habit result = habitService.updateHabit(1L, updates);

        assertHabitEquals(updates, habit);
        verify(habitRepository, times(1)).save(habit);
    }

    @Test
    void testUpdateHabitNonExistent() {
        Long id = 99L;
        Habit updates = Habit.builder().name("New Name").description("New description").build();
        when(habitRepository.findById(id)).thenReturn(Optional.empty());
        assertHabitNotFound(id, () -> habitService.updateHabit(id, updates));
        verify(habitRepository, times(0)).save(any(Habit.class));

    }

    @Test
    void testDeleteHabit() {
        Long id = 1L;
        when(habitRepository.findById(id)).thenReturn(Optional.of(habit));
        doNothing().when(habitRepository).delete(habit);
        habitService.deleteHabit(id);
        verify(habitRepository, times(1)).delete(habit);
    }

    @Test
    void testDeleteHabitNonExistent() {
        Long id = 99L;
        when(habitRepository.findById(id)).thenReturn(Optional.empty());
        assertHabitNotFound(id, () -> habitService.deleteHabit(id));
        verify(habitRepository, times(0)).delete(habit);
    }

    private void assertHabitNotFound(Long id, Executable executable) {
        HabitNotFoundException exception = Assertions.assertThrows(HabitNotFoundException.class, executable);
        Assertions.assertEquals("Habit with id " + id + " not found", exception.getMessage());
        verify(habitRepository, times(1)).findById(id);
    }

    private void assertHabitEquals(Habit expected, Habit actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.getDescription(), actual.getDescription());
    }
}
