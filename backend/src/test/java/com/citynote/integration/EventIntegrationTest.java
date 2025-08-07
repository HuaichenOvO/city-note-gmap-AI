package com.citynote.integration;

import com.citynote.entity.CountyEntity;
import com.citynote.entity.EventEntity;
import com.citynote.entity.User;
import com.citynote.entity.UserProfile;
import com.citynote.entity.enums.EventType;
import com.citynote.repository.CountyRepository;
import com.citynote.repository.EventRepository;
import com.citynote.repository.UserRepository;
import com.citynote.security.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EntityScan(basePackages = "com.citynote.entity")
@EnableJpaRepositories(basePackages = "com.citynote.repository")
public class EventIntegrationTest {

    @Mock
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CountyRepository countyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private User testUser;
    private User otherUser;
    private UserProfile testUserProfile;
    private CountyEntity testCounty;
    private EventEntity testEvent;
    private String validToken;
    private String otherUserToken;

    @BeforeEach
    void setUp() {
// Clean up repositories
        eventRepository.deleteAll();
        userRepository.deleteAll();
        countyRepository.deleteAll();

// Create test county
        testCounty = new CountyEntity();
        testCounty.setId(1);
        testCounty.setCountyName("Test County");
        testCounty.setCountyState("Test State");
        testCounty.setCountyKey("Test county key");
        testCounty = countyRepository.save(testCounty);

// Create test users
        testUser = new User();
        testUser.setUsername("eventuser");
        testUser.setEmail("eventuser@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);

        testUserProfile = new UserProfile();
        testUserProfile.setId(1);
        testUserProfile.setUser(testUser);

        otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("otheruser@example.com");
        otherUser.setPassword(passwordEncoder.encode("password123"));
        otherUser = userRepository.save(otherUser);

// Generate tokens
        validToken = "test-user";
        otherUserToken = "other-user";

// Create test event
        testEvent = new EventEntity();
        testEvent.setTitle("Test Event");
        testEvent.setContent("Test Description");
        testEvent.setEventType(EventType.TEXT);
        testEvent.setCounty(testCounty);
        testEvent.setCreateDate(LocalDateTime.now());
        testEvent.setLastUpdateDate(LocalDateTime.now());
        testEvent.setUserProfile(testUserProfile);
        testEvent = eventRepository.save(testEvent);
    }

    @Test
    void testCreateEvent_Success() throws Exception {
        String eventJson = """
            {
                "title": "New Event",
                "description": "New Event Description",
                "eventType": "FESTIVAL",
                "location": "New Location",
                "eventTime": "2024-12-25T10:00:00",
                "countyId": %d
            }
            """.formatted(testCounty.getId());

        mockMvc.perform(post("/api/event")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void testCreateEvent_WithoutAuth() throws Exception {
        String eventJson = """
            {
                "title": "New Event",
                "description": "New Event Description",
                "eventType": "FESTIVAL",
                "location": "New Location",
                "eventTime": "2024-12-25T10:00:00",
                "countyId": %d
            }
            """.formatted(testCounty.getId());

        mockMvc.perform(post("/api/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetEventsByCounty_Success() throws Exception {
        mockMvc.perform(get("/api/event/county/" + testCounty.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].title", is("Test Event")));
    }

    @Test
    void testGetEventsByUser_Success() throws Exception {
        mockMvc.perform(get("/api/event/user/" + testUser.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].title", is("Test Event")));
    }

    @Test
    void testGetCurrentUserEvents_Success() throws Exception {
        mockMvc.perform(get("/api/event/user/me")
                        .header("Authorization", "Bearer " + validToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].title", is("Test Event")));
    }

    @Test
    void testGetCurrentUserEvents_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/event/user/me")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateEvent_Success() throws Exception {
        String updateJson = """
            {
                "title": "Updated Event",
                "description": "Updated Description",
                "eventType": "SPORTS",
                "location": "Updated Location",
                "eventTime": "2024-12-26T15:00:00",
                "countyId": %d
            }
            """.formatted(testCounty.getId());

        mockMvc.perform(put("/api/event/" + testEvent.getId())
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateEvent_UnauthorizedUser() throws Exception {
        String updateJson = """
            {
                "title": "Updated Event",
                "description": "Updated Description",
                "eventType": "SPORTS",
                "location": "Updated Location",
                "eventTime": "2024-12-26T15:00:00",
                "countyId": %d
            }
            """.formatted(testCounty.getId());

        mockMvc.perform(put("/api/event/" + testEvent.getId())
                        .header("Authorization", "Bearer " + otherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateEvent_NonExistentEvent() throws Exception {
        String updateJson = """
            {
                "title": "Updated Event",
                "description": "Updated Description",
                "eventType": "SPORTS",
                "location": "Updated Location",
                "eventTime": "2024-12-26T15:00:00",
                "countyId": %d
            }
            """.formatted(testCounty.getId());

        mockMvc.perform(put("/api/event/99999")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteEvent_Success() throws Exception {
        mockMvc.perform(delete("/api/event/" + testEvent.getId())
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(true)));
    }

    @Test
    void testDeleteEvent_UnauthorizedUser() throws Exception {
        mockMvc.perform(delete("/api/event/" + testEvent.getId())
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteEvent_NonExistentEvent() throws Exception {
        mockMvc.perform(delete("/api/event/99999")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testToggleEventLike_Success() throws Exception {
        mockMvc.perform(put("/api/event/" + testEvent.getId() + "/like")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(true)));
    }

    @Test
    void testToggleEventLike_WithoutAuth() throws Exception {
        mockMvc.perform(put("/api/event/" + testEvent.getId() + "/like"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testToggleEventLike_NonExistentEvent() throws Exception {
        mockMvc.perform(put("/api/event/99999/like")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCanUserModifyEvent_OwnerUser() throws Exception {
        mockMvc.perform(get("/api/event/" + testEvent.getId() + "/can-modify")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(true)));
    }

    @Test
    void testCanUserModifyEvent_NonOwnerUser() throws Exception {
        mockMvc.perform(get("/api/event/" + testEvent.getId() + "/can-modify")
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(false)));
    }

    @Test
    void testCanUserModifyEvent_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/event/" + testEvent.getId() + "/can-modify"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testPaginationParameters() throws Exception {
        // Create multiple events for pagination testing
        for (int i = 0; i < 15; i++) {
            EventEntity event = new EventEntity();
            event.setTitle("Test Event" + i);
            event.setContent("Test Description" + i);
            event.setEventType(EventType.TEXT);
            event.setCounty(testCounty);
            event.setCreateDate(LocalDateTime.now().minusDays(i));
            event.setLastUpdateDate(LocalDateTime.now().minusDays(i));
            event.setUserProfile(testUserProfile);
//
//            EventEntity event = new EventEntity();
//            event.setTitle("Event " + i);
//            event.setDescription("Description " + i);
//            event.setEventType(EventType.CONCERT);
//            event.setLocation("Location " + i);
//            event.setEventTime(LocalDateTime.now().plusDays(i + 1));
//            event.setCountyId(testCounty.getId());
//            event.setUserId(testUser.getId());
//            event.setCreatedAt(LocalDateTime.now());
//            event.setUpdatedAt(LocalDateTime.now());
            eventRepository.save(event);
        }

        mockMvc.perform(get("/api/event/county/" + testCounty.getId())
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", greaterThan(15)))
                .andExpect(jsonPath("$.totalPages", greaterThan(3)));
    }
}