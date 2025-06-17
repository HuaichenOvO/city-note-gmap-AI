package com.citynote.service;

import com.citynote.dto.EventResponseDTO;
import com.citynote.dto.EventRequestDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface EventService {
    Optional<EventResponseDTO> getEventById(int id);
    List<EventResponseDTO> getEventsByCounty(String countyId); // a list of county-aggregated
    List<EventResponseDTO> getUserPostedEvents(Long userId); // a list of user-posted history
    int postEvent(EventRequestDTO eventRequestDTO); // return new event's ID
    int updateEvent(EventRequestDTO eventRequestDTO); // return new event's ID
    Boolean deleteEvent(int eventId); // true if successfully deleted
}
