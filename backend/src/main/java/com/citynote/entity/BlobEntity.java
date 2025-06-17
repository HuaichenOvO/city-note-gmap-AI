package com.citynote.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "blobs")
public class BlobEntity {
    @Id
    private Long id;
    private String s3Link;
    private String s3Key;
    private int inPlaceOrder;
    @ManyToOne
    @JoinColumn(name = "")
    private EventEntity event;

}
