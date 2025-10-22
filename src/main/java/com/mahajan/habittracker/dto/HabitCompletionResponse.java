package com.mahajan.habittracker.dto;

import com.mahajan.habittracker.model.HabitCompletion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class HabitCompletionResponse {

    private Long habitId;
    private String habitName;
    private String habitDescription;
    private LocalDate completionDate;

    public static HabitCompletionResponse fromEntity(HabitCompletion completion) {
        return HabitCompletionResponse.builder()
                .habitId(completion.getHabit().getId())
                .habitName(completion.getHabit().getName())
                .habitDescription(completion.getHabit().getDescription())
                .completionDate(completion.getCompletionDate())
                .build();
    }
}
