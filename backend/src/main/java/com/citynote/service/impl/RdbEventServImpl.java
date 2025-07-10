package com.citynote.service.impl;

import com.citynote.dto.EventRequestDTO;
import com.citynote.dto.EventResponseDTO;
import com.citynote.entity.BlobEntity;
import com.citynote.entity.CountyEntity;
import com.citynote.entity.EventEntity;
import com.citynote.entity.EventLikeEntity;
import com.citynote.entity.UserProfile;
import com.citynote.entity.enums.EventType;
import com.citynote.repository.*;
import com.citynote.service.EventService;
import com.citynote.security.JwtTokenUtil;
import com.sun.jdi.request.EventRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Qualifier("RdbEventServiceImpl")
public class RdbEventServImpl implements EventService {

    private final EventRepository eventRepository;
    private final BlobRepository blobRepository;
    private final UserProfileRepository userProfileRepository;
    private final CountyRepository countyRepository;
    private final EventLikeRepository eventLikeRepository;

    @Autowired
    public RdbEventServImpl(EventRepository eventRepository,
                            BlobRepository blobRepository,
                            UserProfileRepository userProfileRepository,
                            CountyRepository countyRepository,
                            EventLikeRepository eventLikeRepository) {
        this.eventRepository = eventRepository;
        this.blobRepository = blobRepository;
        this.userProfileRepository = userProfileRepository;
        this.countyRepository = countyRepository;
        this.eventLikeRepository = eventLikeRepository;
    }

    public Optional<EventResponseDTO> getEventById(int id){
        return eventRepository
                .findById(id)
                .map(this::DTOConverter);
    }

    @Transactional
    public Page<EventResponseDTO> getPagesOfEventsByCounty(int countyId, Pageable pageable){
        return eventRepository
                .findByCounty_Id(countyId, pageable)
                .map(this::DTOConverter);
    }

    @Transactional
    public Page<EventResponseDTO> getPagesOfUserPostedEvents(Long userId, Pageable pageable){
        // may have potential value risks
        int userProfileId = userId.intValue();
        return eventRepository
                .findByUserProfile_Id(userProfileId, pageable)
                .map(this::DTOConverter);
    }

    @Transactional
    public Page<EventResponseDTO> getPagesOfCurrentUserEvents(Pageable pageable){
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            throw new RuntimeException("Authentication required. Please login first.");
        }
        
        String currentUsername = authentication.getName();
        
        // Find user profile by username
        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUsername(currentUsername);
        if (userProfileOpt.isPresent()) {
            return eventRepository
                    .findByUserProfile_Id(userProfileOpt.get().getId(), pageable)
                    .map(this::DTOConverter);
        } else {
            throw new RuntimeException("User profile not found for username: " + currentUsername);
        }
    }

    @Transactional
    public int postEvent(EventRequestDTO eventRequestDTO){
        EventEntity eventEntity = new EventEntity();
        eventEntity.setTitle(eventRequestDTO.getTitle());
        eventEntity.setContent(eventRequestDTO.getContent());
        eventEntity.setCreateDate(LocalDateTime.now());
        eventEntity.setLastUpdateDate(LocalDateTime.now());
        
        // Get current authenticated user - require authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            throw new RuntimeException("Authentication required. Please login first.");
        }
        
        String currentUsername = authentication.getName();
        System.out.println("Creating event for user: " + currentUsername);
        
        // Find user profile by username
        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUsername(currentUsername);
        if (userProfileOpt.isPresent()) {
            eventEntity.setUserProfile(userProfileOpt.get());
            System.out.println("Found user profile: " + userProfileOpt.get().getId());
        } else {
            throw new RuntimeException("User profile not found for username: " + currentUsername);
        }
        
        // Set county - get countyId from EventRequestDTO
        if (eventRequestDTO.getCountyId() != null) {
            // Try to find county with the provided ID first
            Optional<CountyEntity> countyOpt = countyRepository.findById(eventRequestDTO.getCountyId());
            if (countyOpt.isPresent()) {
                eventEntity.setCounty(countyOpt.get());
            } else {
                // If not found, try to find by converting the ID format
                // Frontend sends countyId like 6085, but database might have 06085
                Integer convertedCountyId = convertCountyIdFormat(eventRequestDTO.getCountyId());
                if (convertedCountyId != null) {
                    Optional<CountyEntity> convertedCountyOpt = countyRepository.findById(convertedCountyId);
                    if (convertedCountyOpt.isPresent()) {
                        eventEntity.setCounty(convertedCountyOpt.get());
                    } else {
                        throw new RuntimeException("County with ID " + eventRequestDTO.getCountyId() + " (converted: " + convertedCountyId + ") not found");
                    }
                } else {
                    throw new RuntimeException("County with ID " + eventRequestDTO.getCountyId() + " not found");
                }
            }
        } else {
            // If no countyId provided, we need to handle this case
            // For now, throw an error to make it explicit that countyId is required
            throw new RuntimeException("County ID is required for creating an event");
        }
        
        if (eventRequestDTO.getPictureLinks().length > 0) {
            eventEntity.setEventType(EventType.IMAGE);
            for (int i = 0; i < eventRequestDTO.getPictureLinks().length; i++) {
                String link = eventRequestDTO.getPictureLinks()[i];
                BlobEntity blobEntity = new BlobEntity();
                // TODO: let the s3 service add the blobs to the repository
                blobEntity.setS3Key(link.length() > 5 ? link : "s3s3s");
                blobEntity.setS3Link(link);
                blobEntity.setInPlaceOrder(i+1);
                blobEntity.setEvent(eventEntity);
                blobRepository.save(blobEntity);
            }
        }
        else if (eventRequestDTO.getVideoLink() != null) {
            eventEntity.setEventType(EventType.VIDEO);
            BlobEntity blobEntity = new BlobEntity();
            String link = eventRequestDTO.getVideoLink();
            blobEntity.setS3Link(link);
            blobEntity.setS3Key(link.length() > 5 ? link : "s3s3s");
            blobEntity.setInPlaceOrder(0);
            blobEntity.setEvent(eventEntity);
        }
        else {
            eventEntity.setEventType(EventType.TEXT);
        }

        return eventRepository.save(eventEntity).getId();
    }

    @Transactional
    public int updateEvent(int eventId, EventRequestDTO eventRequestDTO){
        // 1. auth
        // 2. data validation
        // 3. fetch object
        // 4. update entity and save it
        // TODO: update links and type

        // 5. return
        return eventRepository.findById(eventId)
                .map(eE -> {
                    eE.setTitle(eventRequestDTO.getTitle());
                    eE.setContent(eventRequestDTO.getContent());
                    eE.setLastUpdateDate(LocalDateTime.now());
                    eventRepository.save(eE);
                    // TODO: update links and type
                    return 1;
                })
                .orElse(-1);
    }

    @Transactional
    public Boolean deleteEvent(int eventId){
        // if no related data in DB, there will be no errors
        Optional<EventEntity> eOptional = eventRepository.findById(eventId);
        if (eOptional.isPresent()) {
            eventRepository.delete(eOptional.get());
            return true;
        }
        return false;
    }

    @Transactional
    public Boolean toggleEventLike(int eventId) {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Authentication required");
        }
        
        String currentUsername = authentication.getName();
        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUsername(currentUsername);
        if (userProfileOpt.isEmpty()) {
            throw new RuntimeException("User profile not found");
        }
        
        UserProfile userProfile = userProfileOpt.get();
        
        // Check if event exists first
        Optional<EventEntity> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isEmpty()) {
            return false;
        }
        
        EventEntity event = eventOptional.get();
        
        // Use a more robust approach to handle concurrent requests
        // First, try to find existing like
        Optional<EventLikeEntity> existingLike = eventLikeRepository.findByEventIdAndUserProfileId(eventId, userProfile.getId());
        
        if (existingLike.isPresent()) {
            // User already liked, so unlike
            EventLikeEntity likeToDelete = existingLike.get();
            eventLikeRepository.delete(likeToDelete);
            
            // Update event likes count atomically
            event.setLikes(Math.max(0, event.getLikes() - 1));
            eventRepository.save(event);
            System.out.println("User " + currentUsername + " unliked event " + eventId);
            return false; // Return false to indicate unliked
        } else {
            // User hasn't liked, so like
            try {
                // Create new like
                EventLikeEntity newLike = new EventLikeEntity();
                newLike.setEvent(event);
                newLike.setUserProfile(userProfile);
                eventLikeRepository.save(newLike);
                
                // Update event likes count
                event.setLikes(event.getLikes() + 1);
                eventRepository.save(event);
                System.out.println("User " + currentUsername + " liked event " + eventId);
                return true; // Return true to indicate liked
            } catch (Exception e) {
                // Handle potential unique constraint violation
                System.out.println("Error creating like, might be duplicate: " + e.getMessage());
                // Check if like was actually created despite the exception
                Optional<EventLikeEntity> checkLike = eventLikeRepository.findByEventIdAndUserProfileId(eventId, userProfile.getId());
                if (checkLike.isPresent()) {
                    // Like was created, update count
                    event.setLikes(event.getLikes() + 1);
                    eventRepository.save(event);
                    return true;
                }
                return false;
            }
        }
    }

    @Override
    public Boolean incrementEventLikes(int eventId) {
        // For backward compatibility, just call toggleEventLike
        return toggleEventLike(eventId);
    }

    /**
     * Check if current user can modify the specified event
     */
    public Boolean canUserModifyEvent(int eventId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            System.out.println("Permission check failed: No authentication");
            return false;
        }

        String currentUsername = authentication.getName();
        System.out.println("Checking permissions for user: " + currentUsername + " on event: " + eventId);
        
        Optional<EventEntity> eventOpt = eventRepository.findById(eventId);
        
        if (eventOpt.isPresent()) {
            EventEntity event = eventOpt.get();
            System.out.println("Found event: " + event.getTitle());
            
            if (event.getUserProfile() != null) {
                System.out.println("Event has user profile: " + event.getUserProfile().getId());
                if (event.getUserProfile().getUser() != null) {
                    String eventOwnerUsername = event.getUserProfile().getUser().getUsername();
                    System.out.println("Event owner: " + eventOwnerUsername);
                    boolean canModify = currentUsername.equals(eventOwnerUsername);
                    System.out.println("Can modify: " + canModify);
                    return canModify;
                } else {
                    System.out.println("Event user profile has no user");
                }
            } else {
                System.out.println("Event has no user profile");
            }
        } else {
            System.out.println("Event not found with ID: " + eventId);
        }
        
        return false;
    }

    private EventResponseDTO DTOConverter(EventEntity e) {
        EventResponseDTO eventResponseDTO = new EventResponseDTO();
        eventResponseDTO.setId(e.getId());
        eventResponseDTO.setTitle(e.getTitle());
        eventResponseDTO.setContent(e.getContent());
        
        // 安全地访问CountyEntity，避免LazyInitializationException
        String countyName = null;
        try {
            countyName = e.getCounty().getCountyName();
        } catch (Exception ex) {
            countyName = "Unknown County";
        }
        eventResponseDTO.setCounty(countyName);
        
        eventResponseDTO.setDate(e.getLastUpdateDate().toString());
        eventResponseDTO.setLikes(e.getLikes());

        if (e.getUserProfile() != null && e.getUserProfile().getUser() != null) {
            eventResponseDTO.setAuthorUsername(e.getUserProfile().getUser().getUsername());
            eventResponseDTO.setAuthorFirstName(e.getUserProfile().getUser().getFirstName());
            eventResponseDTO.setAuthorLastName(e.getUserProfile().getUser().getLastName());
        } else {
            eventResponseDTO.setAuthorUsername("Unknown User");
            eventResponseDTO.setAuthorFirstName("");
            eventResponseDTO.setAuthorLastName("");
        }

        if (e.getEventType() == EventType.IMAGE) {
            String[] pictureLinks = e.getBlobs()
                    .stream()
                    .sorted((u1, u2) -> u1.getInPlaceOrder() - u2.getInPlaceOrder())
                    .map(BlobEntity::getS3Link)
                    .toArray(String[]::new);
            eventResponseDTO.setPictureLinks(pictureLinks);
            eventResponseDTO.setVideoLink("");
        }
        else if (e.getEventType() == EventType.VIDEO) {
            String videoLink = e.getBlobs()
                    .get(0)
                    .getS3Link();
            eventResponseDTO.setPictureLinks(new String[0]);
            eventResponseDTO.setVideoLink(videoLink);
        }
        else {
            eventResponseDTO.setPictureLinks(new String[0]);
            eventResponseDTO.setVideoLink("");
        }

        eventResponseDTO.setEventType(e.getEventType());
        return eventResponseDTO;
    }
    
    /**
     * Convert county ID from frontend format to database format
     * Frontend sends countyId like 6085, but database might have 06085
     * This method tries different format conversions
     */
    private Integer convertCountyIdFormat(Integer countyId) {
        if (countyId == null) {
            return null;
        }
        
        String countyIdStr = countyId.toString();
        
        // If it's already 5 digits, return as is
        if (countyIdStr.length() == 5) {
            return countyId;
        }
        
        // If it's 4 digits, try adding leading zero
        if (countyIdStr.length() == 4) {
            String convertedStr = "0" + countyIdStr;
            try {
                Integer convertedId = Integer.parseInt(convertedStr);
                // Check if this converted ID exists in the database
                Optional<CountyEntity> countyOpt = countyRepository.findById(convertedId);
                if (countyOpt.isPresent()) {
                    return convertedId;
                }
            } catch (NumberFormatException e) {
                // Ignore and continue to next conversion
            }
        }
        
        // If it's 3 digits, try adding two leading zeros
        if (countyIdStr.length() == 3) {
            String convertedStr = "00" + countyIdStr;
            try {
                Integer convertedId = Integer.parseInt(convertedStr);
                // Check if this converted ID exists in the database
                Optional<CountyEntity> countyOpt = countyRepository.findById(convertedId);
                if (countyOpt.isPresent()) {
                    return convertedId;
                }
            } catch (NumberFormatException e) {
                // Ignore and continue to next conversion
            }
        }
        
        return null;
    }
}
