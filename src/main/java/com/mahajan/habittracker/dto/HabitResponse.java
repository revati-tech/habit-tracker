package com.mahajan.habittracker.dto;

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
}
