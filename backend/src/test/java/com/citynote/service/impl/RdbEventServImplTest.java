package com.citynote.service.impl;

import com.citynote.dto.EventRequestDTO;
import com.citynote.dto.EventResponseDTO;
import com.citynote.entity.*;
import com.citynote.entity.enums.EventType;
import com.citynote.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RdbEventServImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private BlobRepository blobRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private CountyRepository countyRepository;

    @Mock
    private EventLikeRepository eventLikeRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RdbEventServImpl eventService;

    private EventEntity testEvent;
    private UserProfile testUserProfile;
    private User testUser;
    private CountyEntity testCounty;
    private EventRequestDTO testEventRequest;

    @BeforeEach
    void setUp() {
        // Reset all mocks before each test
        reset(eventRepository, blobRepository, userProfileRepository, countyRepository, eventLikeRepository);

        // Setup test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        // Setup test user profile
        testUserProfile = new UserProfile();
        testUserProfile.setId(1);
        testUserProfile.setUser(testUser);

        // Setup test county
        testCounty = new CountyEntity();
        testCounty.setId(6085);
        testCounty.setCountyName("Test County");

        // Setup test event
        testEvent = new EventEntity();
        testEvent.setId(1);
        testEvent.setTitle("Test Event");
        testEvent.setContent("Test Content");
        testEvent.setEventType(EventType.TEXT);
        testEvent.setUserProfile(testUserProfile);
        testEvent.setCounty(testCounty);
        testEvent.setLikes(5);
        testEvent.setCreateDate(LocalDateTime.now());
        testEvent.setLastUpdateDate(LocalDateTime.now());
        testEvent.setBlobs(Collections.emptyList());

        // Setup test event request
        testEventRequest = new EventRequestDTO();
        testEventRequest.setTitle("Test Event");
        testEventRequest.setContent("Test Content");
        testEventRequest.setCountyId(6085);
        testEventRequest.setPictureLinks(new String[0]);
    }

    @Test
    void getEventById_Success() {
        // Given
        when(eventRepository.findById(1)).thenReturn(Optional.of(testEvent));

        // When
        Optional<EventResponseDTO> result = eventService.getEventById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Event", result.get().getTitle());
        assertEquals("Test Content", result.get().getContent());
        assertEquals("Test County", result.get().getCounty());
        assertEquals("testuser", result.get().getAuthorUsername());
        verify(eventRepository).findById(1);
    }

    @Test
    void getEventById_NotFound() {
        // Given
        when(eventRepository.findById(1)).thenReturn(Optional.empty());

        // When
        Optional<EventResponseDTO> result = eventService.getEventById(1);

        // Then
        assertFalse(result.isPresent());
        verify(eventRepository).findById(1);
    }

    @Test
    void getPagesOfEventsByCounty_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<EventEntity> eventPage = new PageImpl<>(Arrays.asList(testEvent));
        when(eventRepository.findByCounty_Id(6085, pageable)).thenReturn(eventPage);

        // When
        Page<EventResponseDTO> result = eventService.getPagesOfEventsByCounty(6085, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Event", result.getContent().get(0).getTitle());
        verify(eventRepository).findByCounty_Id(6085, pageable);
    }

    @Test
    void getPagesOfUserPostedEvents_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<EventEntity> eventPage = new PageImpl<>(Arrays.asList(testEvent));
        when(eventRepository.findByUserProfile_Id(1, pageable)).thenReturn(eventPage);

        // When
        Page<EventResponseDTO> result = eventService.getPagesOfUserPostedEvents(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Event", result.getContent().get(0).getTitle());
        verify(eventRepository).findByUserProfile_Id(1, pageable);
    }

    @Test
    void getPagesOfCurrentUserEvents_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<EventEntity> eventPage = new PageImpl<>(Arrays.asList(testEvent));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserProfile));
            when(eventRepository.findByUserProfile_Id(1, pageable)).thenReturn(eventPage);

            // When
            Page<EventResponseDTO> result = eventService.getPagesOfCurrentUserEvents(pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("Test Event", result.getContent().get(0).getTitle());
            verify(userProfileRepository).findByUsername("testuser");
            verify(eventRepository).findByUserProfile_Id(1, pageable);
        }
    }

    @Test
    void getPagesOfCurrentUserEvents_NotAuthenticated() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> eventService.getPagesOfCurrentUserEvents(pageable));
            assertEquals("Authentication required. Please login first.", exception.getMessage());
        }
    }

    @Test
    void postEvent_TextEvent_Success() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserProfile));
            when(countyRepository.findById(testEventRequest.getCountyId())).thenReturn(Optional.of(testCounty));
            when(eventRepository.save(any(EventEntity.class))).thenReturn(testEvent);

            // When
            int result = eventService.postEvent(testEventRequest);

            // Then
            assertEquals(1, result);
            verify(userProfileRepository).findByUsername("testuser");
            verify(countyRepository).findById(testEventRequest.getCountyId());
            verify(eventRepository).save(any(EventEntity.class));
            verify(blobRepository, never()).save(any(BlobEntity.class));
        }
    }

    @Test
    void postEvent_ImageEvent_Success() {
        // Given
        EventRequestDTO imageEventRequest = new EventRequestDTO();
        imageEventRequest.setTitle("Test Image Event");
        imageEventRequest.setContent("Test Content");
        imageEventRequest.setCountyId(6085);
        imageEventRequest.setPictureLinks(new String[]{"image1.jpg", "image2.jpg"});

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserProfile));
            when(countyRepository.findById(imageEventRequest.getCountyId())).thenReturn(Optional.of(testCounty));
            when(eventRepository.save(any(EventEntity.class))).thenReturn(testEvent);
            when(blobRepository.save(any(BlobEntity.class))).thenReturn(new BlobEntity());

            // When
            int result = eventService.postEvent(imageEventRequest);

            // Then
            assertEquals(1, result);
            verify(userProfileRepository).findByUsername("testuser");
            verify(countyRepository).findById(imageEventRequest.getCountyId());
            verify(eventRepository).save(any(EventEntity.class));
            verify(blobRepository, times(2)).save(any(BlobEntity.class));
        }
    }

    @Test
    void postEvent_CountyNotFound() {
        // Given
        EventRequestDTO countyNotFoundRequest = new EventRequestDTO();
        countyNotFoundRequest.setTitle("Test Event");
        countyNotFoundRequest.setContent("Test Content");
        countyNotFoundRequest.setCountyId(9999); // Different county ID
        countyNotFoundRequest.setPictureLinks(new String[0]);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserProfile));
            when(countyRepository.findById(countyNotFoundRequest.getCountyId())).thenReturn(Optional.empty());

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> eventService.postEvent(countyNotFoundRequest));
//            System.out.println(exception.getMessage());
            assertTrue(exception.getMessage().contains("County with ID 9999"));
        }
    }

    @Test
    void postEvent_NoAuthentication() {
        // Given
        EventRequestDTO noAuthRequest = new EventRequestDTO();
        noAuthRequest.setTitle("Test Event");
        noAuthRequest.setContent("Test Content");
        noAuthRequest.setCountyId(6085);
        noAuthRequest.setPictureLinks(new String[0]);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> eventService.postEvent(noAuthRequest));
            assertEquals("Authentication required. Please login first.", exception.getMessage());
        }
    }

    @Test
    void updateEvent_Success() {
        // Given
        when(eventRepository.findById(1)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(EventEntity.class))).thenReturn(testEvent);

        // When
        int result = eventService.updateEvent(1, testEventRequest);

        // Then
        assertEquals(1, result);
        verify(eventRepository).findById(1);
        verify(eventRepository, times(2)).save(any(EventEntity.class));
        verify(blobRepository).deleteByEventId(1);
    }

    @Test
    void updateEvent_EventNotFound() {
        // Given
        when(eventRepository.findById(1)).thenReturn(Optional.empty());

        // When
        int result = eventService.updateEvent(1, testEventRequest);

        // Then
        assertEquals(-1, result);
        verify(eventRepository).findById(1);
        verify(eventRepository, never()).save(any(EventEntity.class));
    }

    @Test
    void deleteEvent_Success() {
        // Given
        when(eventRepository.findById(1)).thenReturn(Optional.of(testEvent));
        when(blobRepository.findBlobEntitiesByEvent(testEvent)).thenReturn(Collections.emptyList());

        // When
        Boolean result = eventService.deleteEvent(1);

        // Then
        assertTrue(result);
        verify(eventRepository).findById(1);
        verify(eventRepository).delete(testEvent);
        verify(blobRepository).findBlobEntitiesByEvent(testEvent);
    }

    @Test
    void deleteEvent_EventNotFound() {
        // Given
        when(eventRepository.findById(1)).thenReturn(Optional.empty());

        // When
        Boolean result = eventService.deleteEvent(1);

        // Then
        assertFalse(result);
        verify(eventRepository).findById(1);
        verify(eventRepository, never()).delete(any(EventEntity.class));
    }

    @Test
    void toggleEventLike_LikeEvent_Success() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserProfile));
            when(eventRepository.findById(1)).thenReturn(Optional.of(testEvent));
            when(eventLikeRepository.findByEventIdAndUserProfileId(1, 1)).thenReturn(Optional.empty());
            when(eventLikeRepository.save(any(EventLikeEntity.class))).thenReturn(new EventLikeEntity());
            when(eventRepository.save(any(EventEntity.class))).thenReturn(testEvent);

            // When
            Boolean result = eventService.toggleEventLike(1);

            // Then
            assertTrue(result);
            verify(eventLikeRepository).save(any(EventLikeEntity.class));
            verify(eventRepository).save(testEvent);
        }
    }

    @Test
    void toggleEventLike_UnlikeEvent_Success() {
        // Given
        EventLikeEntity existingLike = new EventLikeEntity();
        existingLike.setEvent(testEvent);
        existingLike.setUserProfile(testUserProfile);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserProfile));
            when(eventRepository.findById(1)).thenReturn(Optional.of(testEvent));
            when(eventLikeRepository.findByEventIdAndUserProfileId(1, 1)).thenReturn(Optional.of(existingLike));
            when(eventRepository.save(any(EventEntity.class))).thenReturn(testEvent);

            // When
            Boolean result = eventService.toggleEventLike(1);

            // Then
            assertFalse(result);
            verify(eventLikeRepository).delete(existingLike);
            verify(eventRepository).save(testEvent);
        }
    }

    @Test
    void toggleEventLike_EventNotFound() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserProfile));
            when(eventRepository.findById(1)).thenReturn(Optional.empty());

            // When
            Boolean result = eventService.toggleEventLike(1);

            // Then
            assertFalse(result);
            verify(eventLikeRepository, never()).save(any(EventLikeEntity.class));
            verify(eventLikeRepository, never()).delete(any(EventLikeEntity.class));
        }
    }

    @Test
    void canUserModifyEvent_Success() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            when(eventRepository.findById(1)).thenReturn(Optional.of(testEvent));

            // When
            Boolean result = eventService.canUserModifyEvent(1);

            // Then
            assertTrue(result);
            verify(eventRepository).findById(1);
        }
    }

    @Test
    void canUserModifyEvent_DifferentUser() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("differentuser");
            when(eventRepository.findById(1)).thenReturn(Optional.of(testEvent));

            // When
            Boolean result = eventService.canUserModifyEvent(1);

            // Then
            assertFalse(result);
            verify(eventRepository).findById(1);
        }
    }

    @Test
    void canUserModifyEvent_NotAuthenticated() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // When
            Boolean result = eventService.canUserModifyEvent(1);

            // Then
            assertFalse(result);
            verify(eventRepository, never()).findById(anyInt());
        }
    }

    @Test
    void canUserModifyEvent_EventNotFound() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            when(eventRepository.findById(1)).thenReturn(Optional.empty());

            // When
            Boolean result = eventService.canUserModifyEvent(1);

            // Then
            assertFalse(result);
            verify(eventRepository).findById(1);
        }
    }

    @Test
    void incrementEventLikes_CallsToggleEventLike() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserProfile));
            when(eventRepository.findById(1)).thenReturn(Optional.of(testEvent));
            when(eventLikeRepository.findByEventIdAndUserProfileId(1, 1)).thenReturn(Optional.empty());
            when(eventLikeRepository.save(any(EventLikeEntity.class))).thenReturn(new EventLikeEntity());
            when(eventRepository.save(any(EventEntity.class))).thenReturn(testEvent);

            // When
            Boolean result = eventService.incrementEventLikes(1);

            // Then
            assertTrue(result);
            verify(eventLikeRepository).save(any(EventLikeEntity.class));
        }
    }
}