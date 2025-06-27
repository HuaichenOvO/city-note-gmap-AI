package com.citynote.service.impl;

import com.citynote.dto.EventRequestDTO;
import com.citynote.dto.EventResponseDTO;
import com.citynote.entity.BlobEntity;
import com.citynote.entity.CountyEntity;
import com.citynote.entity.EventEntity;
import com.citynote.entity.enums.EventType;
import com.citynote.repository.*;
import com.citynote.service.EventService;
import com.sun.jdi.request.EventRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    public RdbEventServImpl(EventRepository eventRepository,
                            BlobRepository blobRepository,
                            UserProfileRepository userProfileRepository) {
        this.eventRepository = eventRepository;
        this.blobRepository = blobRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public Optional<EventResponseDTO> getEventById(int id){
        return eventRepository
                .findById(id)
                .map(this::DTOConverter);
    }

    public Page<EventResponseDTO> getPagesOfEventsByCounty(int countyId, Pageable pageable){
        return eventRepository
                .findByCounty_Id(countyId, pageable)
                .map(this::DTOConverter);
    }

    public Page<EventResponseDTO> getPagesOfUserPostedEvents(Long userId, Pageable pageable){
        // may have potential value risks
        int userProfileId = userId.intValue();
        return eventRepository
                .findByUserProfile_Id(userProfileId, pageable)
                .map(this::DTOConverter);
    }

    public int postEvent(EventRequestDTO eventRequestDTO){
        EventEntity eventEntity = new EventEntity();
        eventEntity.setTitle(eventRequestDTO.getTitle());
        eventEntity.setContent(eventRequestDTO.getContent());
        eventEntity.setCreateDate(LocalDateTime.now());
        eventEntity.setLastUpdateDate(LocalDateTime.now());
        // TODO: get the current user's userID
        eventEntity.setUserProfile(userProfileRepository.findById(1).get());
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

    public Boolean deleteEvent(int eventId){
        // if no related data in DB, there will be no errors
        Optional<EventEntity> eOptional = eventRepository.findById(eventId);
        if (eOptional.isPresent()) {{
            eventRepository.delete(eOptional.get());}
            return true;
        }
        return false;
    }

    private EventResponseDTO DTOConverter(EventEntity e) {
        EventResponseDTO eventResponseDTO = new EventResponseDTO();
        eventResponseDTO.setId(e.getId());
        eventResponseDTO.setTitle(e.getTitle());
        eventResponseDTO.setContent(e.getContent());
        eventResponseDTO.setCounty(e.getCounty().getCountyName());
        eventResponseDTO.setDate(e.getLastUpdateDate().toString());

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
