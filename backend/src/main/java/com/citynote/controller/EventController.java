package com.citynote.controller;

import com.citynote.dto.EventResponseDTO;
import com.citynote.dto.EventRequestDTO;
import com.citynote.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/event")
public class EventController {

    private final EventService eventService;

    public EventController(@Qualifier("RdbEventServiceImpl") EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<Integer> createEvent(@Validated @RequestBody EventRequestDTO eventRequestDTO) {
        int eventId = eventService.postEvent(eventRequestDTO);
        return ResponseEntity.ok(eventId);
    }

    @GetMapping("/county/{countyId}")
    public ResponseEntity<Page<EventResponseDTO>> getEventsByCounty(
            @PathVariable int countyId,
            @RequestParam int page,
            @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EventResponseDTO> events = eventService.getPagesOfEventsByCounty(countyId, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<EventResponseDTO>> getEventsByUser(
            @PathVariable Long userId,
            @RequestParam int page,
            @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EventResponseDTO> events = eventService.getPagesOfUserPostedEvents(userId, pageable);
        return ResponseEntity.ok(events);
    }

    @PutMapping("/{id}")
    // Pre authorize
    public ResponseEntity<Integer> updateEvent(@PathVariable int id, @RequestParam EventRequestDTO eventRequestDTO) {
        // TODO: remove redundant existence check (either controller or service)
        return eventService.getEventById(id)
                .map(evt ->
                        ResponseEntity.ok(eventService.updateEvent(id, eventRequestDTO))
                )
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    // Pre authorize
    public ResponseEntity<Boolean> deleteEvent(@PathVariable int id) {
        // TODO: remove redundant existence check (either controller or service)
        return eventService.getEventById(id)
                .map(
                        evt -> ResponseEntity.ok(eventService.deleteEvent(id))
                )
                .orElse(ResponseEntity.notFound().build());
    }

}

