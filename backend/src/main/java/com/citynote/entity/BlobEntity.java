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

    @Column(nullable = false, updatable = false)
    private String filename; // 本地图片文件名

    // inplace order would not be updated, and if new pictures are inserted or 
    // old pictures are deleted, the whole list of pictures will be re-ordered 
    // based on existing pictures' auto-incrementing in_place_order
    @Column(nullable = false, updatable = false, length = 5)
    private int inPlaceOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "event_id", updatable = false)
    private EventEntity event;

    // Manual getters and setters for Lombok compatibility
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getInPlaceOrder() {
        return inPlaceOrder;
    }

    public void setInPlaceOrder(int inPlaceOrder) {
        this.inPlaceOrder = inPlaceOrder;
    }

    public EventEntity getEvent() {
        return event;
    }

    public void setEvent(EventEntity event) {
        this.event = event;
    }
}
