package com.citynote.repository;

import com.citynote.entity.EventLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EventLikeRepository extends JpaRepository<EventLikeEntity, Integer> {
    int countByEventId(int eventId);
    
    @Query("SELECT el FROM EventLikeEntity el WHERE el.event.id = :eventId AND el.userProfile.id = :userProfileId")
    Optional<EventLikeEntity> findByEventIdAndUserProfileId(@Param("eventId") int eventId, @Param("userProfileId") int userProfileId);
    
    boolean existsByEventIdAndUserProfileId(int eventId, int userProfileId);
}
