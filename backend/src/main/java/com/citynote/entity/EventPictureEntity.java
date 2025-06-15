package com.citynote.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class EventPictureEntity {
    @Id
    private Long id;
    private Long eventId;
    private String pictureLink;
}
