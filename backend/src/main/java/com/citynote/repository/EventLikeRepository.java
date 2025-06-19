package com.citynote.repository;

import com.citynote.entity.EventEntity;
import com.citynote.entity.EventLikeEntity;
import com.citynote.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLikeRepository extends JpaRepository<EventLikeEntity, Integer> {
    int countByEventId(int eventId);
}
