package com.bankapp.localbankapp.unit;

import com.BankApp.localbankapp.model.User;
import com.BankApp.localbankapp.repository.UserRepository;
import com.BankApp.localbankapp.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Brazhkin
 */
@DisplayName("User service testing")
@Tag("UserService")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setPassword("testPass");
        testUser.setEmail("testUser@test.com");
    }

    @Test
    void getUserByIdSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User user = userService.getUserById(1L);
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("testUser", user.getUsername());
        assertEquals("testPass", user.getPassword());
    }

    @Test
    void getUserByIdThrowsAEmptyResultDataAccessException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EmptyResultDataAccessException.class, () -> userService.getUserById(1L));
    }

    @Test
    void getAllUsersSuccess() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        List<User> users = userService.getAllUsers();
        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals(1L, users.getFirst().getId());
        assertEquals("testUser", users.getFirst().getUsername());
    }

    @Test
    void getAllUsersReturnsEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        List<User> users = userService.getAllUsers();
        assertNotNull(users);
        assertEquals(0, users.size());
    }

    @Test
    void updateUserSuccess() {
        User newUser = new User();
        newUser.setId(10L);
        newUser.setUsername("newUser");
        newUser.setPassword("newPass");
        newUser.setEmail("newUser@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, newUser);
        assertNotNull(updated);
        assertEquals(1L, updated.getId());
        assertEquals("newUser", updated.getUsername());
        assertEquals("newPass", updated.getPassword());
        assertEquals("newUser@test.com", updated.getEmail());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateNonExistingUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EmptyResultDataAccessException.class, () -> userService.updateUser(1L, testUser));
    }

    @Test
    void loadUserByUsernameSuccess() {
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        UserDetails user = userService.loadUserByUsername(testUser.getUsername());
        assertNotNull(user);
        assertEquals(testUser.getUsername(), user.getUsername());
        assertEquals(testUser.getPassword(), user.getPassword());
        assertLinesMatch(user.getAuthorities().stream().map(String::valueOf).toList(), List.of("ROLE_USER"));
    }

    @Test
    void loadUserByUsernameThrowsAccountNotFoundException() {
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.empty());
        String testUsername = testUser.getUsername();
        assertThrows(BadCredentialsException.class, () -> userService.loadUserByUsername(testUsername));
    }
}
