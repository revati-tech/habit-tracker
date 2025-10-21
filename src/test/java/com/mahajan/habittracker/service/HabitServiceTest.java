package com.mahajan.habittracker.service;

import com.mahajan.habittracker.exceptions.HabitNotFoundException;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.model.User;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitServiceTest {
    private static final Long TEST_HABIT_ID = 10L;

    @Mock
    private HabitRepository habitRepository;
    @InjectMocks
    private HabitService habitService;

    private Habit habit;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@test.com").build();
        habit = Habit.builder().id(TEST_HABIT_ID).name("Exercise").description("Daily workout").build();
    }

    @Test
    void testGetHabitsForUser() {
        List<Habit> habits = List.of(
                habit,
                Habit.builder().name("Meditation").description("Daily 10 min meditation")
                        .build());
        when(habitRepository.findByUser(user)).thenReturn(habits);

        List<Habit> result = habitService.getHabitsForUser(user);

        Assertions.assertEquals(2, result.size());
        assertHabitEquals(habits.get(0), result.get(0));
        assertHabitEquals(habits.get(1), result.get(1));
    }

    @Test
    void testGetHabitByIdForUser() {
        when(habitRepository.findByIdAndUser(TEST_HABIT_ID, user)).thenReturn(Optional.of(habit));
        Habit result = habitService.getHabitByIdForUser(TEST_HABIT_ID, user);
        assertHabitEquals(habit, result);
    }

    @Test
    void testCreateHabitForUser() {
        Habit savedHabit = Habit.builder().id(TEST_HABIT_ID).name("Exercise").description("Daily workout").build();

        when(habitRepository.save(any(Habit.class))).thenReturn(savedHabit);

        Habit result = habitService.createHabitForUser(habit, user);

        Assertions.assertNotNull(result);
        assertHabitEquals(savedHabit, result);
        verify(habitRepository, times(1)).save(habit);
    }

    @Test
    void testUpdateHabitForUser() {
        Habit updates = Habit.builder().id(TEST_HABIT_ID).name("New Name").description("New description").build();

        when(habitRepository.findByIdAndUser(TEST_HABIT_ID, user)).thenReturn(Optional.of(habit));
        when(habitRepository.save(any(Habit.class))).thenReturn(habit);

        Habit result = habitService.updateHabitForUser(updates, user);

        assertHabitEquals(habit, result);
        verify(habitRepository, times(1)).save(habit);
    }

    @Test
    void testDeleteHabitForUser() {
        when(habitRepository.findByIdAndUser(TEST_HABIT_ID, user))
                .thenReturn(Optional.of(habit));
        doNothing().when(habitRepository).delete(habit);
        habitService.deleteHabitForUser(TEST_HABIT_ID, user);
        verify(habitRepository, times(1)).delete(habit);
    }

    @Test
    void testDeleteHabitNotFound() {
        when(habitRepository.findByIdAndUser(TEST_HABIT_ID, user))
                .thenReturn(Optional.empty());
        assertHabitNotFound(() -> habitService.deleteHabitForUser(TEST_HABIT_ID, user));
        verify(habitRepository, times(0)).delete(habit);
    }

    @Test
    void testGetHabitByIdNotFound() {
        when(habitRepository.findByIdAndUser(TEST_HABIT_ID, user))
                .thenReturn(Optional.empty());

        assertThrows(HabitNotFoundException.class,
                () -> habitService.getHabitByIdForUser(TEST_HABIT_ID, user));

        verify(habitRepository, times(1)).findByIdAndUser(TEST_HABIT_ID, user);
    }

    @Test
    void testGetHabitsForUserWithNoHabits() {
        when(habitRepository.findByUser(user)).thenReturn(List.of());
        List<Habit> result = habitService.getHabitsForUser(user);
        Assertions.assertTrue(result.isEmpty());
        verify(habitRepository, times(1)).findByUser(user);
    }

    private void assertHabitNotFound(Executable executable) {
        HabitNotFoundException exception = assertThrows(HabitNotFoundException.class, executable);
        Assertions.assertEquals(
                String.format("Habit with id=%s not found for user with email=%s", TEST_HABIT_ID, user.getEmail()),
                exception.getMessage()
        );
        verify(habitRepository, times(1)).
                findByIdAndUser(TEST_HABIT_ID, user);
    }

    private void assertHabitEquals(Habit expected, Habit actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.getDescription(), actual.getDescription());
    }
}
