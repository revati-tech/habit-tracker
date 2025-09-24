package com.mahajan.habittracker.model;

import lombok.Value;

@Value(staticConstructor = "of")
public class HabitKey {
    Long userId;
    Long habitId;
}
