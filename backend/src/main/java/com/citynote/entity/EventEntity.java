package com.citynote.entity;

import com.citynote.dto.EventType;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Entity
@Getter
@Setter
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createDate;

    @Column(nullable = false)
    private LocalDateTime LastUpdateDate;

    @Column(nullable = false)
    private String county;

    private String pictureArrayLinks;

    private String videoLink;

    @Column(nullable = false)
    private int eventType;
}
