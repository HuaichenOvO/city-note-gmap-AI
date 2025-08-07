package com.citynote.service.impl;

import com.citynote.entity.User;
import com.citynote.entity.UserProfile;
import com.citynote.repository.UserRepository;
import com.citynote.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserProfile testUserProfile;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("plainPassword");

        testUserProfile = new UserProfile();
        testUserProfile.setId(1);
        testUserProfile.setUser(testUser);
    }

    @Test
    void createUser_ShouldEncodePasswordAndCreateUserProfile() {
        // Given
        String encodedPassword = "encodedPassword123";
        User userToCreate = new User();
        userToCreate.setUsername("newuser");
        userToCreate.setEmail("new@example.com");
        userToCreate.setPassword("plainPassword");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setEmail("new@example.com");
        savedUser.setPassword(encodedPassword);

        when(passwordEncoder.encode("plainPassword")).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // When
        User result = userService.createUser(userToCreate);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("newuser", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertEquals(encodedPassword, result.getPassword());

        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(userToCreate);
        verify(userProfileRepository).save(any(UserProfile.class));

        // Verify that the password was set on the user before saving
        assertEquals(encodedPassword, userToCreate.getPassword());
    }

    @Test
    void createUser_ShouldCreateUserProfileWithCorrectUser() {
        // Given
        User userToCreate = new User();
        userToCreate.setUsername("newuser");
        userToCreate.setPassword("plainPassword");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // When
        userService.createUser(userToCreate);

        // Then
        verify(userProfileRepository).save(argThat(profile ->
                profile.getUser().equals(savedUser)
        ));
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserById(userId);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserByUsername_WhenUserExists_ShouldReturnUser() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserByUsername(username);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void getUserByUsername_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserByUsername(username);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void getUserByEmail_WhenUserExists_ShouldReturnUser() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserByEmail(email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getUserByEmail_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserByEmail(email);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");

        List<User> expectedUsers = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedUsers, result);
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of());

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void updateUser_WithPassword_ShouldEncodeNewPassword() {
        // Given
        User userToUpdate = new User();
        userToUpdate.setId(1L);
        userToUpdate.setUsername("updateduser");
        userToUpdate.setPassword("newPlainPassword");

        String encodedPassword = "newEncodedPassword";
        when(passwordEncoder.encode("newPlainPassword")).thenReturn(encodedPassword);
        when(userRepository.save(userToUpdate)).thenReturn(userToUpdate);

        // When
        User result = userService.updateUser(userToUpdate);

        // Then
        assertNotNull(result);
        assertEquals(encodedPassword, userToUpdate.getPassword());
        verify(passwordEncoder).encode("newPlainPassword");
        verify(userRepository).save(userToUpdate);
    }

    @Test
    void updateUser_WithNullPassword_ShouldNotEncodePassword() {
        // Given
        User userToUpdate = new User();
        userToUpdate.setId(1L);
        userToUpdate.setUsername("updateduser");
        userToUpdate.setPassword(null);

        when(userRepository.save(userToUpdate)).thenReturn(userToUpdate);

        // When
        User result = userService.updateUser(userToUpdate);

        // Then
        assertNotNull(result);
        assertNull(userToUpdate.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(userToUpdate);
    }

    @Test
    void updateUser_WithEmptyPassword_ShouldNotEncodePassword() {
        // Given
        User userToUpdate = new User();
        userToUpdate.setId(1L);
        userToUpdate.setUsername("updateduser");
        userToUpdate.setPassword("");

        when(userRepository.save(userToUpdate)).thenReturn(userToUpdate);

        // When
        User result = userService.updateUser(userToUpdate);

        // Then
        assertNotNull(result);
        assertEquals("", userToUpdate.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(userToUpdate);
    }

    @Test
    void updateUser_WithWhitespacePassword_ShouldEncodePassword() {
        // Given
        User userToUpdate = new User();
        userToUpdate.setId(1L);
        userToUpdate.setPassword("   ");

        String encodedPassword = "encodedWhitespace";
        when(passwordEncoder.encode("   ")).thenReturn(encodedPassword);
        when(userRepository.save(userToUpdate)).thenReturn(userToUpdate);

        // When
        User result = userService.updateUser(userToUpdate);

        // Then
        assertNotNull(result);
        assertEquals(encodedPassword, userToUpdate.getPassword());
        verify(passwordEncoder).encode("   ");
        verify(userRepository).save(userToUpdate);
    }

    @Test
    void deleteUser_ShouldCallRepositoryDeleteById() {
        // Given
        Long userId = 1L;

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_WithNullId_ShouldStillCallRepository() {
        // Given
        Long userId = null;

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).deleteById(userId);
    }
}