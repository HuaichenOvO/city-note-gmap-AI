package com.citynote.service.impl;

import com.citynote.dto.EventRequestDTO;
import com.citynote.dto.EventResponseDTO;
import com.citynote.entity.BlobEntity;
import com.citynote.entity.enums.EventType;
import com.citynote.repository.EventRepository;
import com.citynote.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Qualifier("RdbEventServiceImpl")
public class RdbEventServImpl implements EventService {

    private final EventRepository eventRepository;

    @Autowired
    public RdbEventServImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Optional<EventResponseDTO> getEventById(int id){
        EventResponseDTO eventResponseDTO = new EventResponseDTO();
        eventRepository.findById(id)
                .ifPresent(e -> {
                    eventResponseDTO.setId(e.getId());
                    eventResponseDTO.setTitle(e.getTitle());
                    eventResponseDTO.setContent(e.getContent());
                    eventResponseDTO.setEventType(e.getEventType());
                    if (e.getEventType() == EventType.IMAGE) {
                        List<BlobEntity> blobs = e.getBlobs();
                        String[] pictureLinks = blobs.stream()
                                .sorted((u1, u2) -> u1.getInPlaceOrder() - u2.getInPlaceOrder())
                                .map(BlobEntity::getS3Link)
                                .toArray(String[]::new);
                    }
                    else if (e.getEventType() == EventType.VIDEO) {int i = 1;}
                    else if (e.getEventType() == EventType.TEXT) {int i = 1;}
                });
        return Optional.empty();
    }

    public List<EventResponseDTO> getEventsByCounty(String countyId){
        return null;
    }

    public List<EventResponseDTO> getUserPostedEvents(Long userId){
        return null;
    }

    public int postEvent(EventRequestDTO eventRequestDTO){
        return 1;
    }

    public int updateEvent(EventRequestDTO eventRequestDTO){
        return 1;
    }

    public Boolean deleteEvent(int eventId){
        return false;
    }
}
