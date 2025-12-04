package com.mahajan.habittracker.service;

import com.mahajan.habittracker.exceptions.HabitAlreadyCompletedException;
import com.mahajan.habittracker.exceptions.HabitCompletionNotFoundException;
import com.mahajan.habittracker.model.Habit;
import com.mahajan.habittracker.model.HabitCompletion;
import com.mahajan.habittracker.model.User;
import com.mahajan.habittracker.repository.HabitCompletionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitCompletionServiceTest {

    private static final Long TEST_HABIT_ID = 100L;
    private static final Long TEST_USER_ID = 200L;
    private static final LocalDate TODAY = LocalDate.now();

    @Mock
    private HabitCompletionRepository completionRepository;

    @InjectMocks
    private HabitCompletionService completionService;

    private Habit habit;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(TEST_USER_ID).email("test@test.com").build();
        habit = Habit.builder().id(TEST_HABIT_ID).name("Exercise").description("Daily workout").build();
    }

    @Test
    void testMarkCompletedSuccess() {
        when(completionRepository.existsByHabitAndUserAndCompletionDate(habit, user, TODAY))
                .thenReturn(false);

        HabitCompletion expected = HabitCompletion.builder()
                .habit(habit)
                .user(user)
                .completionDate(TODAY)
                .build();

        when(completionRepository.save(any(HabitCompletion.class))).thenReturn(expected);

        HabitCompletion result = completionService.markCompleted(habit, user, TODAY);

        assertNotNull(result);
        assertCompletionEquals(expected, result);

        verify(completionRepository, times(1))
                .existsByHabitAndUserAndCompletionDate(habit, user, TODAY);
        verify(completionRepository, times(1)).save(any(HabitCompletion.class));
    }

    @Test
    void testMarkCompletedAlreadyExists() {
        when(completionRepository.existsByHabitAndUserAndCompletionDate(habit, user, TODAY)).thenReturn(true);

        assertHabitAlreadyCompleted(() -> completionService.markCompleted(habit, user, TODAY));

        verify(completionRepository, times(1))
                .existsByHabitAndUserAndCompletionDate(habit, user, TODAY);
        verify(completionRepository, never()).save(any(HabitCompletion.class));
    }

    @Test
    void testGetCompletions() {
        HabitCompletion c1 = HabitCompletion.builder().habit(habit).user(user).completionDate(TODAY).build();
        HabitCompletion c2 = HabitCompletion.builder().habit(habit).user(user).completionDate(TODAY.minusDays(1)).build();

        when(completionRepository.findAllByHabitAndUserOrderByCompletionDateDesc(habit, user))
                .thenReturn(List.of(c1, c2));

        List<HabitCompletion> result = completionService.getAllCompletionsForHabit(habit, user);

        assertEquals(2, result.size());
        assertCompletionEquals(c1, result.get(0));
        assertCompletionEquals(c2, result.get(1));

        verify(completionRepository, times(1))
                .findAllByHabitAndUserOrderByCompletionDateDesc(habit, user);
    }

    @Test
    void testGetCompletionsEmpty() {
        when(completionRepository.findAllByHabitAndUserOrderByCompletionDateDesc(habit, user))
                .thenReturn(List.of());

        List<HabitCompletion> result = completionService.getAllCompletionsForHabit(habit, user);

        assertTrue(result.isEmpty());
        verify(completionRepository, times(1))
                .findAllByHabitAndUserOrderByCompletionDateDesc(habit, user);
    }

    @Test
    void testUnmarkCompletedSuccess() {
        HabitCompletion completion = HabitCompletion.builder()
                .id(1L)
                .habit(habit)
                .user(user)
                .completionDate(TODAY)
                .build();

        when(completionRepository.findByHabitAndUserAndCompletionDate(habit, user, TODAY))
                .thenReturn(Optional.of(completion));

        completionService.unmarkCompleted(habit, user, TODAY);

        verify(completionRepository, times(1))
                .findByHabitAndUserAndCompletionDate(habit, user, TODAY);
        verify(completionRepository, times(1)).delete(completion);
    }

    @Test
    void testUnmarkCompletedNotFound() {
        when(completionRepository.findByHabitAndUserAndCompletionDate(habit, user, TODAY))
                .thenReturn(Optional.empty());

        assertHabitCompletionNotFound(() -> completionService.unmarkCompleted(habit, user, TODAY));

        verify(completionRepository, times(1))
                .findByHabitAndUserAndCompletionDate(habit, user, TODAY);
        verify(completionRepository, never()).delete(any(HabitCompletion.class));
    }

    // ------------------------------------------------------------
    // Helper assertions
    // ------------------------------------------------------------

    private void assertHabitAlreadyCompleted(Executable executable) {
        HabitAlreadyCompletedException exception =
                assertThrows(HabitAlreadyCompletedException.class, executable);

        assertTrue(exception.getMessage().contains("already completed"));
    }

    private void assertHabitCompletionNotFound(Executable executable) {
        HabitCompletionNotFoundException exception =
                assertThrows(HabitCompletionNotFoundException.class, executable);
        assertTrue(exception.getMessage().contains("No completion found"));
    }

    private void assertCompletionEquals(HabitCompletion expected, HabitCompletion actual) {
        assertEquals(expected.getHabit(), actual.getHabit());
        assertEquals(expected.getUser(), actual.getUser());
        assertEquals(expected.getCompletionDate(), actual.getCompletionDate());
    }
}
