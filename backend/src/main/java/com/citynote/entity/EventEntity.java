package com.citynote.entity;

import com.citynote.entity.enums.EventType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "events")
@DynamicInsert
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private int id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @OneToMany(fetch = FetchType.EAGER,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "event"
    )
    private List<BlobEntity> blobs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private EventType eventType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "county_id")
    private CountyEntity county;

    @ColumnDefault("0")
    @Column(nullable = false)
    private int likes;

    @Column(nullable = false)
    private LocalDateTime createDate;

    private LocalDateTime lastUpdateDate;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_profile_id")
    private UserProfile userProfile;

    @OneToMany(mappedBy = "event", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<EventLikeEntity> eventLikes;

    // Manual getters and setters for Lombok compatibility
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<BlobEntity> getBlobs() {
        return blobs;
    }

    public void setBlobs(List<BlobEntity> blobs) {
        this.blobs = blobs;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public CountyEntity getCounty() {
        return county;
    }

    public void setCounty(CountyEntity county) {
        this.county = county;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
}
