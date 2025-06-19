package com.citynote.repository;

import com.citynote.entity.BlobEntity;
import com.citynote.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlobRepository extends JpaRepository<BlobEntity, Integer> {
    Optional<BlobEntity> findById(int id);

    List<BlobEntity> findBlobEntitiesByEvent(EventEntity event);

    List<BlobEntity> findBlobEntitiesByEvent_Id(int eventId);
}
