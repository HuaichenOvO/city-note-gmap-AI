package com.citynote.service;

import com.citynote.entity.User;
import com.citynote.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword123");
    }

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword123", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());

        // Verify authorities
        Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("USER")));

        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_WhenUserDoesNotExist_ShouldThrowUsernameNotFoundException() {
        // Given
        String username = "nonexistentuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(username)
        );

        assertEquals("User not found with username: " + username, exception.getMessage());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_WithNullUsername_ShouldThrowUsernameNotFoundException() {
        // Given
        String username = null;
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(username)
        );

        assertEquals("User not found with username: " + username, exception.getMessage());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_WithEmptyUsername_ShouldThrowUsernameNotFoundException() {
        // Given
        String username = "";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(username)
        );

        assertEquals("User not found with username: " + username, exception.getMessage());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_WithWhitespaceUsername_ShouldHandleCorrectly() {
        // Given
        String username = "   ";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(username)
        );

        assertEquals("User not found with username: " + username, exception.getMessage());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_WhenUserHasEmptyPassword_ShouldReturnUserDetailsWithEmptyPassword() {
        // Given
        String username = "testuser";
        testUser.setPassword("");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("", result.getPassword());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_ShouldAlwaysReturnUserDetailsWithUserAuthority() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Then
        Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());

        GrantedAuthority authority = authorities.iterator().next();
        assertEquals("USER", authority.getAuthority());

        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_ShouldReturnEnabledUserDetails() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Then
        assertTrue(result.isEnabled(), "User should be enabled");
        assertTrue(result.isAccountNonExpired(), "Account should not be expired");
        assertTrue(result.isAccountNonLocked(), "Account should not be locked");
        assertTrue(result.isCredentialsNonExpired(), "Credentials should not be expired");

        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_CaseSensitiveUsername_ShouldPassExactUsernameToRepository() {
        // Given
        String username = "TestUser"; // Mixed case
        User mixedCaseUser = new User();
        mixedCaseUser.setUsername("TestUser");
        mixedCaseUser.setPassword("password123");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mixedCaseUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals("TestUser", result.getUsername());
        verify(userRepository).findByUsername("TestUser"); // Exact case should be passed
    }

    @Test
    void loadUserByUsername_ShouldCallRepositoryOnlyOnce() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        customUserDetailsService.loadUserByUsername(username);

        // Then
        verify(userRepository, times(1)).findByUsername(username);
        verifyNoMoreInteractions(userRepository);
    }
}