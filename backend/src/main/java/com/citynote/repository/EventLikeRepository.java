package com.citynote.repository;

import com.citynote.entity.EventLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLikeRepository extends JpaRepository<EventLikeEntity, Integer> {
    int countByEventId(int eventId);
}
