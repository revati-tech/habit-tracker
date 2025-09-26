package com.mahajan.habittracker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @Column(nullable = false, length = 255)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

}
