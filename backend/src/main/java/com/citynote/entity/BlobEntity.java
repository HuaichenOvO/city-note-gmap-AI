package com.citynote.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "blobs")
public class BlobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blob_id")
    private int id;

    @Column(nullable = false, updatable = false, length = 255)
    private String s3Link;

    @Column(nullable = false, updatable = false, length = 255)
    private String s3Key;

    @Column(nullable = false, updatable = false, length = 5)
    private int inPlaceOrder;

    @ManyToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "event_id", updatable = false)
    private EventEntity event;

}
