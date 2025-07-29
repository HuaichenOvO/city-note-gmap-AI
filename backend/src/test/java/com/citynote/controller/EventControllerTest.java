package com.citynote.controller;

import com.citynote.dto.EventRequestDTO;
import com.citynote.dto.EventResponseDTO;
import com.citynote.entity.enums.EventType;
import com.citynote.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createEvent_WithValidData_ShouldReturnEventId() throws Exception {
        // Arrange
        EventRequestDTO eventRequest = createTestEventRequestDTO();
        int expectedEventId = 123;

        when(eventService.postEvent(any(EventRequestDTO.class))).thenReturn(expectedEventId);

        // Act & Assert
        mockMvc.perform(post("/api/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedEventId)));

        verify(eventService).postEvent(any(EventRequestDTO.class));
    }

    @Test
    void getEventsByCounty_WithValidCountyId_ShouldReturnPagedEvents() throws Exception {
        // Arrange
        int countyId = 1;
        int page = 0;
        int size = 10;
        List<EventResponseDTO> events = Arrays.asList(
                createTestEventResponseDTO(1, "Event 1"),
                createTestEventResponseDTO(2, "Event 2")
        );
        Page<EventResponseDTO> eventPage = new PageImpl<>(events, PageRequest.of(page, size), events.size());

        when(eventService.getPagesOfEventsByCounty(eq(countyId), any(Pageable.class)))
                .thenReturn(eventPage);

        // Act & Assert
        mockMvc.perform(get("/api/event/county/{countyId}", countyId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Event 1"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].title").value("Event 2"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.number").value(page));

        verify(eventService).getPagesOfEventsByCounty(eq(countyId), any(Pageable.class));
    }

    @Test
    void getEventsByUser_WithValidUserId_ShouldReturnPagedEvents() throws Exception {
        // Arrange
        Long userId = 1L;
        int page = 0;
        int size = 5;
        List<EventResponseDTO> events = List.of(createTestEventResponseDTO(1, "User Event"));
        Page<EventResponseDTO> eventPage = new PageImpl<>(events, PageRequest.of(page, size), events.size());

        when(eventService.getPagesOfUserPostedEvents(eq(userId), any(Pageable.class)))
                .thenReturn(eventPage);

        // Act & Assert
        mockMvc.perform(get("/api/event/user/{userId}", userId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("User Event"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(eventService).getPagesOfUserPostedEvents(eq(userId), any(Pageable.class));
    }

    @Test
    void getCurrentUserEvents_WithValidPagination_ShouldReturnPagedEvents() throws Exception {
        // Arrange
        int page = 1;
        int size = 20;
        List<EventResponseDTO> events = List.of(createTestEventResponseDTO(1, "My Event"));
        Page<EventResponseDTO> eventPage = new PageImpl<>(events, PageRequest.of(page, size), events.size());

        when(eventService.getPagesOfCurrentUserEvents(any(Pageable.class)))
                .thenReturn(eventPage);

        // Act & Assert
        mockMvc.perform(get("/api/event/user/me")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("My Event"));

        verify(eventService).getPagesOfCurrentUserEvents(any(Pageable.class));
    }

    @Test
    void updateEvent_WithValidPermissionsAndExistingEvent_ShouldReturnUpdatedEventId() throws Exception {
        // Arrange
        int eventId = 1;
        EventRequestDTO eventRequest = createTestEventRequestDTO();
        EventResponseDTO existingEvent = createTestEventResponseDTO(eventId, "Existing Event");

        when(eventService.canUserModifyEvent(eventId)).thenReturn(true);
        when(eventService.getEventById(eventId)).thenReturn(Optional.of(existingEvent));
        when(eventService.updateEvent(eq(eventId), any(EventRequestDTO.class))).thenReturn(eventId);

        // Act & Assert
        mockMvc.perform(put("/api/event/{id}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(eventId)));

        verify(eventService).canUserModifyEvent(eventId);
        verify(eventService).getEventById(eventId);
        verify(eventService).updateEvent(eq(eventId), any(EventRequestDTO.class));
    }

    @Test
    void updateEvent_WithoutPermissions_ShouldReturnForbidden() throws Exception {
        // Arrange
        int eventId = 1;
        EventRequestDTO eventRequest = createTestEventRequestDTO();

        when(eventService.canUserModifyEvent(eventId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(put("/api/event/{id}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isForbidden());

        verify(eventService).canUserModifyEvent(eventId);
        verify(eventService, never()).getEventById(eventId);
        verify(eventService, never()).updateEvent(anyInt(), any(EventRequestDTO.class));
    }

    @Test
    void updateEvent_WithNonExistentEvent_ShouldReturnNotFound() throws Exception {
        // Arrange
        int eventId = 999;
        EventRequestDTO eventRequest = createTestEventRequestDTO();

        when(eventService.canUserModifyEvent(eventId)).thenReturn(true);
        when(eventService.getEventById(eventId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/event/{id}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isNotFound());

        verify(eventService).canUserModifyEvent(eventId);
        verify(eventService).getEventById(eventId);
        verify(eventService, never()).updateEvent(anyInt(), any(EventRequestDTO.class));
    }

    @Test
    void deleteEvent_WithValidPermissionsAndExistingEvent_ShouldReturnTrue() throws Exception {
        // Arrange
        int eventId = 1;
        EventResponseDTO existingEvent = createTestEventResponseDTO(eventId, "Event to Delete");

        when(eventService.canUserModifyEvent(eventId)).thenReturn(true);
        when(eventService.getEventById(eventId)).thenReturn(Optional.of(existingEvent));
        when(eventService.deleteEvent(eventId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/event/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(eventService).canUserModifyEvent(eventId);
        verify(eventService).getEventById(eventId);
        verify(eventService).deleteEvent(eventId);
    }

    @Test
    void deleteEvent_WithoutPermissions_ShouldReturnForbidden() throws Exception {
        // Arrange
        int eventId = 1;

        when(eventService.canUserModifyEvent(eventId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/event/{id}", eventId))
                .andExpect(status().isForbidden());

        verify(eventService).canUserModifyEvent(eventId);
        verify(eventService, never()).getEventById(eventId);
        verify(eventService, never()).deleteEvent(eventId);
    }

    @Test
    void deleteEvent_WithNonExistentEvent_ShouldReturnNotFound() throws Exception {
        // Arrange
        int eventId = 999;

        when(eventService.canUserModifyEvent(eventId)).thenReturn(true);
        when(eventService.getEventById(eventId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/event/{id}", eventId))
                .andExpect(status().isNotFound());

        verify(eventService).canUserModifyEvent(eventId);
        verify(eventService).getEventById(eventId);
        verify(eventService, never()).deleteEvent(eventId);
    }

    @Test
    void toggleEventLike_WithExistingEvent_ShouldReturnToggleResult() throws Exception {
        // Arrange
        int eventId = 1;
        EventResponseDTO existingEvent = createTestEventResponseDTO(eventId, "Event to Like");

        when(eventService.getEventById(eventId)).thenReturn(Optional.of(existingEvent));
        when(eventService.toggleEventLike(eventId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(put("/api/event/{id}/like", eventId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(eventService).getEventById(eventId);
        verify(eventService).toggleEventLike(eventId);
    }

    @Test
    void toggleEventLike_WithNonExistentEvent_ShouldReturnNotFound() throws Exception {
        // Arrange
        int eventId = 999;

        when(eventService.getEventById(eventId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/event/{id}/like", eventId))
                .andExpect(status().isNotFound());

        verify(eventService).getEventById(eventId);
        verify(eventService, never()).toggleEventLike(eventId);
    }

    @Test
    void canUserModifyEvent_WithValidEventId_ShouldReturnPermissionStatus() throws Exception {
        // Arrange
        int eventId = 1;

        when(eventService.canUserModifyEvent(eventId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/event/{id}/can-modify", eventId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(eventService).canUserModifyEvent(eventId);
    }

    @Test
    void canUserModifyEvent_WithNoPermissions_ShouldReturnFalse() throws Exception {
        // Arrange
        int eventId = 1;

        when(eventService.canUserModifyEvent(eventId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/event/{id}/can-modify", eventId))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(eventService).canUserModifyEvent(eventId);
    }

    private EventRequestDTO createTestEventRequestDTO() {
        EventRequestDTO dto = new EventRequestDTO();
        // Set properties based on your actual EventRequestDTO structure
        dto.setTitle("test title");
        dto.setContent("test content");
        dto.setCounty("example county");
        dto.setCountyId(62012);
        dto.setPictureLinks(new String[0]);
        dto.setVideoLink(null);
        return dto;
    }

    private EventResponseDTO createTestEventResponseDTO(int id, String title) {
        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(id);
        dto.setTitle(title);
        dto.setContent("test content");
        dto.setCounty("example county");
        dto.setEventType(EventType.TEXT);
        dto.setPictureLinks(new String[0]);
        dto.setVideoLink(null);
        return dto;
    }
}