package com.mahajan.habittracker.service;

import com.mahajan.habittracker.model.User;
import com.mahajan.habittracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        User result = userService.getUserByEmail(userEmail).get();
        assertUser(user, result);
        verify(userRepository, times(1)).findByEmail(userEmail);
    }

    private void assertUser(User expected, User actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getEmail(), actual.getEmail());
    }
}
