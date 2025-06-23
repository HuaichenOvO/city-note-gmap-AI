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

    Page<EventEntity> findByCounty(CountyEntity county, Pageable pageable);

    Page<EventEntity> findByUserProfile(UserProfile userProfile, Pageable pageable);

    Page<EventEntity> findByCounty_Name(String countyName, Pageable pageable);

    Page<EventEntity> findByCounty_State(String stateName, Pageable pageable);

    Page<EventEntity> findByUserProfile_Id(int userProfileId, Pageable pageable);

    Page<EventEntity> findByCounty_NameAndUserProfile_Id(String countyName, int userProfileId, Pageable pageable);

    Page<EventEntity> findByCounty_StateAndUserProfile_Id(String stateName, int userProfileId, Pageable pageable);

    Page<EventEntity> findByLastUpdateDateAfter(LocalDateTime lastUpdateDateAfter, Pageable pageable);

}
