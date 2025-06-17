package com.citynote.service.impl;

import com.citynote.dto.EventRequestDTO;
import com.citynote.dto.EventResponseDTO;
import com.citynote.service.EventService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Qualifier("RdbEventServiceImpl")
public class RdbEventServImpl implements EventService {
    public Optional<EventResponseDTO> getEventById(int id){
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
