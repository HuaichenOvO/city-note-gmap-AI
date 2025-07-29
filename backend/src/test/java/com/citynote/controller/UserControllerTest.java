package com.citynote.controller;

import com.citynote.entity.User;
import com.citynote.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private Authentication authentication;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

//        // Setup default authentication mock
//        Collection<SimpleGrantedAuthority> authorities =
//                List.of(new SimpleGrantedAuthority("ROLE_USER"));
//
////        when(authentication.getAuthorities()).thenReturn((Collection<? extends GrantedAuthority>) authorities);
//        doReturn(authorities).when(authentication).getAuthorities();
//        when(authentication.isAuthenticated()).thenReturn(true);
//        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    @WithMockUser(roles = "USER")
    void createUser_WithValidUser_ShouldReturnCreatedUser() throws Exception {
        User inputUser = createTestUser();
        inputUser.setId(null); // New user shouldn't have ID

        User savedUser = createTestUser();
        savedUser.setId(1L);

        when(userService.createUser(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/users")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).createUser(any(User.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserById_WithExistingUser_ShouldReturnUser() throws Exception {
        Long userId = 1L;
        User user = createTestUser();
        user.setId(userId);

        when(userService.getUserById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).getUserById(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserById_WithNonExistentUser_ShouldReturnNotFound() throws Exception {
        Long userId = 999L;

        when(userService.getUserById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        User user1 = createTestUser();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");

        User user2 = createTestUser();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");

        List<User> users = Arrays.asList(user1, user2);

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].username").value("user2"))
                .andExpect(jsonPath("$[1].email").value("user2@example.com"));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_WithEmptyList_ShouldReturnEmptyArray() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateUser_WithExistingUser_ShouldReturnUpdatedUser() throws Exception {
        Long userId = 1L;
        User existingUser = createTestUser();
        existingUser.setId(userId);

        User updateRequest = createTestUser();
        updateRequest.setUsername("updateduser");
        updateRequest.setEmail("updated@example.com");

        User updatedUser = createTestUser();
        updatedUser.setId(userId);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");

        when(userService.getUserById(userId)).thenReturn(Optional.of(existingUser));
        when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userService).getUserById(userId);
        verify(userService).updateUser(any(User.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateUser_WithNonExistentUser_ShouldReturnNotFound() throws Exception {
        Long userId = 999L;
        User updateRequest = createTestUser();

        when(userService.getUserById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(userId);
        verify(userService, never()).updateUser(any(User.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUser_WithExistingUser_ShouldReturnOk() throws Exception {
        Long userId = 1L;
        User existingUser = createTestUser();
        existingUser.setId(userId);

        when(userService.getUserById(userId)).thenReturn(Optional.of(existingUser));
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isOk());

        verify(userService).getUserById(userId);
        verify(userService).deleteUser(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUser_WithNonExistentUser_ShouldReturnNotFound() throws Exception {
        Long userId = 999L;

        when(userService.getUserById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(userId);
        verify(userService, never()).deleteUser(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void createUser_WithNullUser_ShouldHandleGracefully() throws Exception {
        when(userService.createUser(any(User.class))).thenThrow(new IllegalArgumentException("User cannot be null"));

        mockMvc.perform(post("/api/users")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(userService).createUser(any(User.class));
    }

    @Test
    void updateUser_EnsuresIdIsSetCorrectly() throws Exception {
        Long userId = 1L;
        User existingUser = createTestUser();
        existingUser.setId(userId);

        User updateRequest = createTestUser();
        updateRequest.setId(999L); // Different ID in request body
        updateRequest.setUsername("updateduser");

        User updatedUser = createTestUser();
        updatedUser.setId(userId); // Should use path variable ID
        updatedUser.setUsername("updateduser");

        when(userService.getUserById(userId)).thenReturn(Optional.of(existingUser));
        when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId)); // Should be path variable ID, not request body ID

        verify(userService).getUserById(userId);
        verify(userService).updateUser(argThat(user -> user.getId().equals(userId)));
    }

    private User createTestUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        return user;
    }
}