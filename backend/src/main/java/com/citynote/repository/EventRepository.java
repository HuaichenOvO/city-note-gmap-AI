package com.citynote.repository;

import com.citynote.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Long> {
    List<EventEntity> findByCounty(String countyName);
}
