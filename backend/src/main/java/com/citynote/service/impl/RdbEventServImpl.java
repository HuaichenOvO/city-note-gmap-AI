package com.citynote.service.impl;

import com.citynote.dto.EventRequestDTO;
import com.citynote.dto.EventResponseDTO;
import com.citynote.entity.BlobEntity;
import com.citynote.entity.CountyEntity;
import com.citynote.entity.EventEntity;
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

    @Autowired
    public RdbEventServImpl(EventRepository eventRepository,
                            BlobRepository blobRepository,
                            UserProfileRepository userProfileRepository,
                            CountyRepository countyRepository) {
        this.eventRepository = eventRepository;
        this.blobRepository = blobRepository;
        this.userProfileRepository = userProfileRepository;
        this.countyRepository = countyRepository;
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
            Optional<CountyEntity> countyOpt = countyRepository.findById(eventRequestDTO.getCountyId());
            if (countyOpt.isPresent()) {
                eventEntity.setCounty(countyOpt.get());
            } else {
                // Use default county if not found
                Optional<CountyEntity> defaultCountyOpt = countyRepository.findById(1013); // Butler County, Alabama
                if (defaultCountyOpt.isPresent()) {
                    eventEntity.setCounty(defaultCountyOpt.get());
                } else {
                    throw new RuntimeException("Default county not found");
                }
            }
        } else {
            // Use default county if no countyId provided
            Optional<CountyEntity> defaultCountyOpt = countyRepository.findById(1013); // Butler County, Alabama
            if (defaultCountyOpt.isPresent()) {
                eventEntity.setCounty(defaultCountyOpt.get());
            } else {
                throw new RuntimeException("Default county not found");
            }
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
    public Boolean incrementEventLikes(int eventId) {
        Optional<EventEntity> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isPresent()) {
            EventEntity event = eventOptional.get();
            event.setLikes(event.getLikes() + 1);
            eventRepository.save(event);
            return true;
        }
        return false;
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

}
