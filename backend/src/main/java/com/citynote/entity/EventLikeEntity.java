package com.citynote.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "event_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "event_id", "user_id" })
})
public class EventLikeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_like_id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private EventEntity event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserProfile userProfile;

    // Manual getters and setters for Lombok compatibility
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public EventEntity getEvent() {
        return event;
    }

    public void setEvent(EventEntity event) {
        this.event = event;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
}
