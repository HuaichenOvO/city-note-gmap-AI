package com.citynote.dto;

import com.citynote.entity.enums.EventType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

// Holds data from backend (database) to frontend
@Data
public class EventResponseDTO {
    private int id;
    private String title;
    private String content;
    private String date;
    private String county;
    private String[] pictureLinks;
    private String videoLink;
    private EventType eventType;
    private int likes;
    private String authorUsername;
    private String authorFirstName;
    private String authorLastName;
}

