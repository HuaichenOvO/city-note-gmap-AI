package com.citynote.controller;

import com.citynote.dto.EventResponseDTO;
import com.citynote.dto.EventRequestDTO;
import com.citynote.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/event")
public class EventController {

    private final EventService eventService;

    public EventController(@Qualifier("DummyEventServiceImpl") EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<Long> createEvent(@Validated @RequestBody EventRequestDTO eventRequestDTO) {
        Long eventId = eventService.postEvent(eventRequestDTO);
        return ResponseEntity.ok(eventId);
    }

    @GetMapping("/county/{countyId}")
    public ResponseEntity<List<EventResponseDTO>> getEventsByCounty(@PathVariable String countyId) {
        List<EventResponseDTO> events = eventService.getEventsByCounty(countyId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EventResponseDTO>> getEventsByUser(@PathVariable Long userId) {
        List<EventResponseDTO> events = eventService.getUserPostedEvents(userId);
        return ResponseEntity.ok(events);
    }

    @PutMapping("/{id}")
    // Pre authorize
    public ResponseEntity<Long> updateEvent(@PathVariable Long id, @RequestParam EventRequestDTO eventRequestDTO) {
        return eventService.getEventById(id)
                .map(evt -> ResponseEntity.ok(eventService.updateEvent(eventRequestDTO)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    // Pre authorize
    public ResponseEntity<Boolean> deleteEvent(@PathVariable Long id) {
        return eventService.getEventById(id)
                .map(evt -> ResponseEntity.ok(eventService.deleteEvent(id)))
                .orElse(ResponseEntity.notFound().build());
    }

}

