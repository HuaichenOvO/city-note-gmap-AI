package com.citynote.service.impl;

import com.citynote.controller.FileUploadController;
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
// import com.citynote.security.JwtTokenUtil;
// import com.sun.jdi.request.EventRequest;
// import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.io.File;
import java.util.stream.Collectors;

@Service
@Qualifier("RdbEventServiceImpl")
public class RdbEventServImpl implements EventService {

    @Value("${file.upload.path:uploads/}")
    private String uploadPath;
    private final Path uploadDir;

//    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    private final EventRepository eventRepository;
    private final BlobRepository blobRepository;
    private final UserProfileRepository userProfileRepository;
    private final CountyRepository countyRepository;
    private final EventLikeRepository eventLikeRepository;

    public RdbEventServImpl(
            @Value("${file.upload.path:uploads/}") String uploadPath,
            EventRepository eventRepository,
            BlobRepository blobRepository,
            UserProfileRepository userProfileRepository,
            CountyRepository countyRepository,
            EventLikeRepository eventLikeRepository) {
        this.uploadDir = Paths.get(System.getProperty("user.dir"), uploadPath).toAbsolutePath();
        this.eventRepository = eventRepository;
        this.blobRepository = blobRepository;
        this.userProfileRepository = userProfileRepository;
        this.countyRepository = countyRepository;
        this.eventLikeRepository = eventLikeRepository;
    }

    public Optional<EventResponseDTO> getEventById(int id) {
        return eventRepository
                .findById(id)
                .map(this::DTOConverter);
    }

    @Transactional
    public Page<EventResponseDTO> getPagesOfEventsByCounty(int countyId, Pageable pageable) {
        return eventRepository
                .findByCounty_Id(countyId, pageable)
                .map(this::DTOConverter);
    }

    @Transactional
    public Page<EventResponseDTO> getPagesOfUserPostedEvents(Long userId, Pageable pageable) {
        // may have potential value risks
        int userProfileId = userId.intValue();
        return eventRepository
                .findByUserProfile_Id(userProfileId, pageable)
                .map(this::DTOConverter);
    }

    @Transactional
    public Page<EventResponseDTO> getPagesOfCurrentUserEvents(Pageable pageable) {
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
    public int postEvent(EventRequestDTO eventRequestDTO) {
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
        System.out.println("[Event Service] Creating event for user: " + currentUsername);

        // Find user profile by username
        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUsername(currentUsername);
        if (userProfileOpt.isPresent()) {
            eventEntity.setUserProfile(userProfileOpt.get());
            System.out.println("[Event Service] Found user profile: " + userProfileOpt.get().getId());
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
                        throw new RuntimeException("County with ID " + eventRequestDTO.getCountyId() + " (converted: "
                                + convertedCountyId + ") not found");
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
                String filename = eventRequestDTO.getPictureLinks()[i];
                BlobEntity blobEntity = new BlobEntity();
                blobEntity.setFilename(filename);
                blobEntity.setInPlaceOrder(i + 1);
                blobEntity.setEvent(eventEntity);
                blobRepository.save(blobEntity);
            }
        } else {
            eventEntity.setEventType(EventType.TEXT);
        }

        return eventRepository.save(eventEntity).getId();
    }

    @Transactional
    public int updateEvent(int eventId, EventRequestDTO eventRequestDTO) {
        // 1. auth
        // 2. data validation
        // 3. fetch object
        // 4. update entity and save it

        Optional<EventEntity> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return -1;
        }

        EventEntity eventEntity = eventOpt.get();

        System.out.printf("""
                [Event Service] Updating event: %s
                Entity picture list: %s
                DTO picture list:%s
                """, eventEntity.getId(),
                Arrays.toString(eventEntity.getBlobs().stream().map(BlobEntity::getFilename).toArray()),
                Arrays.toString(eventRequestDTO.getPictureLinks()));

        // Update basic fields
        eventEntity.setTitle(eventRequestDTO.getTitle());
        eventEntity.setContent(eventRequestDTO.getContent());
        eventEntity.setLastUpdateDate(LocalDateTime.now());

        eventEntity = eventRepository.save(eventEntity);

        // 使用JPQL直接删除blobs，避免级联问题
        // use JPQL to delete blobs directly, preventing cascade problems
        blobRepository.deleteByEventId(eventId);

        // Update event type and blobs based on content
        if (eventRequestDTO.getPictureLinks() != null && eventRequestDTO.getPictureLinks().length > 0) {
            eventEntity.setEventType(EventType.IMAGE);
            for (int i = 0; i < eventRequestDTO.getPictureLinks().length; i++) {
                String filename = eventRequestDTO.getPictureLinks()[i];
                BlobEntity blobEntity = new BlobEntity();
                blobEntity.setFilename(filename);
                blobEntity.setInPlaceOrder(i + 1);
                blobEntity.setEvent(eventEntity);
                blobRepository.save(blobEntity);
            }
        } else {
            eventEntity.setEventType(EventType.TEXT);
        }

        // 最后再次保存event
        eventRepository.save(eventEntity);
        return 1;
    }

    @Transactional
    public Boolean deleteEvent(int eventId) {
        System.out.println("[Event Service] Starting deleteEvent for eventId: " + eventId);
        System.out.println("[Event Service] uploadDir: " + uploadDir);
        
        // if no related data in DB, there will be no errors
        Optional<EventEntity> eOptional = eventRepository.findById(eventId);
        if (eOptional.isPresent()) {
            String rootDir = System.getProperty("user.dir");
            System.out.println("[Event Service] rootDir: " + rootDir);
            EventEntity event = eOptional.get();
            List<BlobEntity> blobs = blobRepository.findBlobEntitiesByEvent(event);
            System.out.println("[Event Service] Found " + blobs.size() + " blobs to delete");
            
            List<String> filenames;

            // get the file names
            try {
                filenames = blobs.stream()
                        .map(s -> {
                            String filename = s.getFilename();
                            Path destPath = uploadDir.resolve(filename).toAbsolutePath();
                            System.out.println("[Event Service] Mapping filename: " + filename + " to path: " + destPath);
                            return destPath.toString();
                        })
                        .toList();
            } catch (Exception e) {
                System.err.println("[Event Service] Error mapping filenames: " + e.getMessage());
                e.printStackTrace();
                return false;
            }

            // delete the files
            try {
                for (String fullPath : filenames) {
                    File targetFile = new File(fullPath);
                    System.out.println("[Event Service] Attempting to delete file: " + fullPath);
                    System.out.println("[Event Service] File exists: " + targetFile.exists());
                    System.out.println("[Event Service] File absolute path: " + targetFile.getAbsolutePath());
                    
                    if (!targetFile.exists()) {
                        System.err.println("[Event Service] File does not exist: " + fullPath);
                        // Don't throw exception, just log and continue
                        continue;
                    }
                    if (targetFile.delete()) {
                        System.out.println("[Event Service] successfully removed file: " + fullPath);
                    } else {
                        System.err.println("[Event Service] Failed to remove file: " + fullPath);
                        // Don't throw exception, just log and continue
                    }
                }
            } catch (Exception e) {
                System.err.println("[Event Service] Error deleting files: " + e.getMessage());
                e.printStackTrace();
                // Don't return false here, continue with entity deletion
            }

            // delete the entities in the repository
            try {
                System.out.println("[Event Service] Deleting blob entities...");
                blobRepository.deleteAll(blobs);
                System.out.println("[Event Service] Deleting event entity...");
                eventRepository.delete(event);
                System.out.println("[Event Service] Event deletion completed successfully");
            } catch (Exception e) {
                System.err.println("[Event Service] Error deleting entities: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
            return true;
        }
        System.out.println("[Event Service] Event not found with id: " + eventId);
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
        Optional<EventLikeEntity> existingLike = eventLikeRepository.findByEventIdAndUserProfileId(eventId,
                userProfile.getId());

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
                Optional<EventLikeEntity> checkLike = eventLikeRepository.findByEventIdAndUserProfileId(eventId,
                        userProfile.getId());
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
        StringBuilder messageBuilder = new StringBuilder("[Event Service]");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {
            messageBuilder.append("\n\tPermission check failed: No authentication");
            System.out.println(messageBuilder);
            return false;
        }

        String currentUsername = authentication.getName();
        messageBuilder.append("\n\tPermissions for user: ").append(currentUsername)
                    .append(" on event: ").append(eventId);

        Optional<EventEntity> eventOpt = eventRepository.findById(eventId);

        if (eventOpt.isPresent()) {
            EventEntity event = eventOpt.get();
            messageBuilder.append("\n\tFound event: ").append(event.getTitle());

            if (event.getUserProfile() != null) {
                messageBuilder.append("\n\tEvent has user profile: ").append(event.getUserProfile().getId());
                if (event.getUserProfile().getUser() != null) {
                    String eventOwnerUsername = event.getUserProfile().getUser().getUsername();
                    messageBuilder.append("\n\tEvent owner: ").append(eventOwnerUsername);
                    boolean canModify = currentUsername.equals(eventOwnerUsername);
                    messageBuilder.append("\n\tCan modify: ").append(canModify);
                    System.out.println(messageBuilder);
                    return canModify;
                } else {
                    messageBuilder.append("\n\tEvent user profile has no user");
                }
            } else {
                messageBuilder.append("\n\tEvent has no user profile");
            }
        } else {
            messageBuilder.append("\n\tEvent not found with ID: ").append(eventId);
        }

        System.out.println(messageBuilder);
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
                    .map(BlobEntity::getFilename)
                    .toArray(String[]::new);
            eventResponseDTO.setPictureLinks(pictureLinks);
            eventResponseDTO.setVideoLink("");
        } else {
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
