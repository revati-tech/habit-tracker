package com.mahajan.habittracker.dto;

import com.mahajan.habittracker.model.Habit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class HabitResponse {
    private Long id;
    private String name;
    private String description;
    private Integer currentStreak;
    private Integer longestStreak;

    public static HabitResponse fromEntity(Habit habit, Integer currentStreak, Integer longestStreak) {
        return HabitResponse.builder()
                .id(habit.getId())
                .name(habit.getName())
                .description(habit.getDescription())
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .build();
    }
}
