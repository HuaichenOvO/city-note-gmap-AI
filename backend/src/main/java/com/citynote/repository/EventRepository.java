package com.citynote.repository;

import com.citynote.entity.CountyEntity;
import com.citynote.entity.EventEntity;
import com.citynote.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EventRepository extends JpaRepository<EventEntity, Integer> {
    Optional<EventEntity> findById(int id);

    Page<EventEntity> findByCounty_Id(int countyId, Pageable pageable);

    Page<EventEntity> findByUserProfile_Id(int userProfileId, Pageable pageable);

    Page<EventEntity> findByCounty_CountyName(String countyName, Pageable pageable);

    Page<EventEntity> findByCounty_CountyState(String stateName, Pageable pageable);

    Page<EventEntity> findByCounty_CountyNameAndUserProfile_Id(String countyName, int userProfileId, Pageable pageable);

    Page<EventEntity> findByCounty_CountyStateAndUserProfile_Id(String stateName, int userProfileId, Pageable pageable);

    Page<EventEntity> findByLastUpdateDateAfter(LocalDateTime lastUpdateDateAfter, Pageable pageable);

}
