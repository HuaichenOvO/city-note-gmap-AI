package com.citynote.dto;

import lombok.Data;

import java.util.Optional;

@Data
public class EventRequestDTO {
    private String title;
    private String content;
    private String county;
    private String[] pictureLinks;
    private String videoLink;
}
