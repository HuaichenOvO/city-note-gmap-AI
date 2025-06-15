package com.citynote.service;

import com.citynote.dto.EventResponseDTO;
import com.citynote.dto.EventRequestDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface EventService {
    Optional<EventResponseDTO> getEventById(Long id);
    List<EventResponseDTO> getEventsByCounty(String countyId); // a list of county-aggregated
    List<EventResponseDTO> getUserPostedEvents(Long userId); // a list of user-posted history
    Long postEvent(EventRequestDTO eventRequestDTO); // return new event's ID
    Long updateEvent(EventRequestDTO eventRequestDTO); // return new event's ID
    Boolean deleteEvent(Long eventId); // true if successfully deleted
}
