package com.mahajan.habittracker.service;

import com.mahajan.habittracker.exceptions.UserNotFoundException;
import com.mahajan.habittracker.model.User;
import com.mahajan.habittracker.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    private final String userEmail = "test@test.com";

    @BeforeEach
    public void setup() {
        user = User.builder().id(1L).email(userEmail).build();
    }

    @Test
    public void testCreateUser() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createUser(user);

        assertNotNull(result);
        assertUser(user, result);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);
        assertUser(user, result);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetUserByEmail() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        User result = userService.getUserByEmail(userEmail);
        assertUser(user, result);
        verify(userRepository, times(1)).findByEmail(userEmail);
    }

    @Test
    void testGetUserByIdNonExistent() {
        Long id = 99L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertUserNotFound(id, () ->   userService.getUserById(id));
    }

    @Test
    void testGetUserByEmailNonExistent() {
        String email = "abc";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.getUserByEmail(email));
        Assertions.assertEquals("User with email " + email + " not found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
    }

    private void assertUser(User expected, User actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getEmail(), actual.getEmail());
    }

    private void assertUserNotFound(Long id, Executable executable) {
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, executable);
        Assertions.assertEquals("User with id " + id + " not found", exception.getMessage());
        verify(userRepository, times(1)).findById(id);
    }
}
