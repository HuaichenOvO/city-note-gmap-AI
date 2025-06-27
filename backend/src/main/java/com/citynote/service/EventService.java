package com.citynote.service;

import com.citynote.dto.EventResponseDTO;
import com.citynote.dto.EventRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface EventService {
    Optional<EventResponseDTO> getEventById(int id);

    // a list of county-aggregated
    Page<EventResponseDTO> getPagesOfEventsByCounty(int countyId, Pageable pageable);

    // a list of user-posted history
    Page<EventResponseDTO> getPagesOfUserPostedEvents(Long userId, Pageable pageable);

    // return new event's ID
    int postEvent(EventRequestDTO eventRequestDTO);

    // get id from controller path variable, return new event's ID
    int updateEvent(int eventId, EventRequestDTO eventRequestDTO);

    // true if successfully deleted
    Boolean deleteEvent(int eventId);
}
