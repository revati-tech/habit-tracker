package com.mahajan.habittracker.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Table(name = "users")  // avoid reserved keyword "user"
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

   @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
   private List<Habit> habits;
}
