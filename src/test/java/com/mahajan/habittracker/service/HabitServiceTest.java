package com.mahajan.habittracker.service;

import com.mahajan.habittracker.exceptions.HabitNotFoundException;
import com.mahajan.habittracker.exceptions.UserNotFoundException;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.model.HabitKey;
import com.mahajan.habittracker.model.User;
import com.mahajan.habittracker.repository.HabitRepository;
import jakarta.transaction.Transactional;
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
@Transactional
class HabitServiceTest {

    @Mock
    private HabitRepository habitRepository;

    @InjectMocks
    private HabitService habitService;

    @Mock
    private UserService userService;

    private Habit habit;

    private User user;

    private static final Long TEST_USER_ID = 1L;

    private static final Long TEST_HABIT_ID = 10L;

    private static final HabitKey TEST_HABIT_KEY =
            HabitKey.of(TEST_USER_ID, TEST_HABIT_ID);

    @BeforeEach
    void setUp() {
        user = User.builder().id(TEST_USER_ID).email("test@test.com").build();
        habit = Habit.builder().id(TEST_HABIT_ID).name("Exercise").description("Daily workout").build();
    }

    @Test
    void testGetHabitsByUser() {
        List<Habit> habits = List.of(
                habit,
                Habit.builder().name("Meditation").description("Daily 10 min meditation")
                        .build());
        when(habitRepository.findAll()).thenReturn(habits);

        List<Habit> result = habitService.getHabitsByUser(TEST_USER_ID);

        Assertions.assertEquals(2, result.size());
        assertHabitEquals(habits.get(0), result.get(0));
        assertHabitEquals(habits.get(1), result.get(1));
    }

    @Test
    void testGetHabitForUserById() {
        when(habitRepository.findByIdAndUserId(TEST_HABIT_ID, TEST_USER_ID)).thenReturn(Optional.of(habit));
        when(userService.getUserById(TEST_USER_ID)).thenReturn(user);
        Habit result = habitService.getHabitForUserById(TEST_HABIT_KEY);
        assertHabitEquals(habit, result);
    }

    @Test
    void testGetHabitForUserByIdNonExistent() {
        when(habitRepository.findByIdAndUserId(TEST_HABIT_ID, TEST_USER_ID))
                .thenReturn(Optional.empty());
        when(userService.getUserById(TEST_USER_ID)).thenReturn(user);
        assertHabitNotFound(() ->   habitService.getHabitForUserById(TEST_HABIT_KEY));
    }

    @Test
    void testCreateHabit() {
        Habit savedHabit = Habit.builder().id(TEST_HABIT_ID).name("Exercise").description("Daily workout").build();

        when(habitRepository.save(any(Habit.class))).thenReturn(savedHabit);
        when(userService.getUserById(TEST_USER_ID)).thenReturn(user);

        Habit result = habitService.createHabit(user.getId(), habit);

        Assertions.assertNotNull(result);
        assertHabitEquals(savedHabit, result);

        verify(habitRepository, times(1)).save(habit);
    }

    @Test
    void testUpdateHabit() {
        Habit updates = Habit.builder().name("New Name").description("New description").build();

        when(habitRepository.findByIdAndUserId(TEST_HABIT_ID, TEST_USER_ID)).thenReturn(Optional.of(habit));
        when(habitRepository.save(any(Habit.class))).thenReturn(habit);
        when(userService.getUserById(TEST_USER_ID)).thenReturn(user);

        Habit result = habitService.updateHabit(TEST_HABIT_KEY, updates);

        assertHabitEquals(habit, result);
        verify(habitRepository, times(1)).save(habit);
    }

    @Test
    void testUpdateHabitNonExistent() {
        Habit updates = Habit.builder().name("New Name").description("New description").build();
        when(userService.getUserById(TEST_USER_ID)).thenReturn(user);
        when(habitRepository.findByIdAndUserId(TEST_HABIT_ID, TEST_USER_ID)).thenReturn(Optional.empty());
        assertHabitNotFound(() -> habitService.updateHabit(TEST_HABIT_KEY, updates));
        verify(habitRepository, times(0)).save(any(Habit.class));

    }

    @Test
    void testDeleteHabit() {
        when(habitRepository.findByIdAndUserId(TEST_HABIT_ID, TEST_USER_ID))
                .thenReturn(Optional.of(habit));
        when(userService.getUserById(TEST_USER_ID)).thenReturn(user);
        doNothing().when(habitRepository).delete(habit);
        habitService.deleteHabit(TEST_HABIT_KEY);
        verify(habitRepository, times(1)).delete(habit);
    }

    @Test
    void testDeleteHabitNonExistent() {
        when(habitRepository.findByIdAndUserId(TEST_HABIT_ID, TEST_USER_ID))
                .thenReturn(Optional.empty());
        when(userService.getUserById(TEST_USER_ID)).thenReturn(user);
        assertHabitNotFound(() -> habitService.deleteHabit(TEST_HABIT_KEY));
        verify(habitRepository, times(0)).delete(habit);
    }

    @Test
    void testGetHabitByIdUserNotFound() {
        Long userId = 2L;
        Long habitId = 2L;

        when(userService.getUserById(anyLong())).thenThrow(new UserNotFoundException(userId));

        Assertions.assertThrows(UserNotFoundException.class, () ->
                habitService.getHabitForUserById(HabitKey.of(userId, habitId)));

        verify(userService, times(1)).getUserById(userId);
        verify(habitRepository, never()).findByIdAndUserId(anyLong(), anyLong());
    }

    private void assertHabitNotFound(Executable executable) {
        HabitNotFoundException exception = Assertions
                .assertThrows(HabitNotFoundException.class, executable);
        Assertions.assertEquals("Habit with id " + TEST_HABIT_ID + " not found", exception.getMessage());
        verify(habitRepository, times(1)).
                findByIdAndUserId(TEST_HABIT_ID, TEST_USER_ID);
    }

    private void assertHabitEquals(Habit expected, Habit actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.getDescription(), actual.getDescription());
    }
}
