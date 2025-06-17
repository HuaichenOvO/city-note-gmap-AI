package com.citynote.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "event_likes")
public class EventLikeEntity {
    @id
    private int id;

    private int eventId;
    private int userId;
}
