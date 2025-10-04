package com.mahajan.habittracker.dto;

import com.mahajan.habittracker.model.Habit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HabitRequest {
    @NotBlank(message = "Habit name must not be blank")
    @Size(max = 100, message = "Habit name must be at most 100 characters")
    private String name;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    // Convert this DTO into a Habit entity
    public Habit toEntity() {
        return Habit.builder()
                .name(this.name)
                .description(this.description)
                .build();
    }
}
