package com.citynote.service.impl;

import com.citynote.dto.EventRequestDTO;
import com.citynote.dto.EventResponseDTO;
import com.citynote.service.EventService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;

@Service
@Qualifier("DummyEventServiceImpl")
public class DummyEventServiceImpl implements EventService {
    @Override
    public Optional<EventResponseDTO> getEventById(int id){
        return Optional.empty();
    }

    public List<EventResponseDTO> getEventsByCounty(String countyId){
        EventResponseDTO myResponse = new EventResponseDTO();
        myResponse.setId(10);
        myResponse.setTitle("Dummy title " + countyId + " - 1");
        myResponse.setContent("Dummy content reslt");
        myResponse.setDate("2025-06-10");
        myResponse.setCounty(countyId);
        myResponse.setPictureLinks(new String[0]);
        myResponse.setVideoLink("");

        EventResponseDTO myResponse2 = new EventResponseDTO();
        myResponse2.setId(11);
        myResponse2.setTitle("Dummy title " + countyId + " - 2");
        myResponse2.setContent("Dummy content reslt");
        myResponse2.setDate("2025-06-10");
        myResponse2.setCounty(countyId);
        myResponse2.setPictureLinks(new String[0]);
        myResponse2.setVideoLink("");
        return new ArrayList<>(List.of(myResponse, myResponse2));
    }

    public List<EventResponseDTO> getUserPostedEvents(Long userId){
        return new ArrayList<>();
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
