package com.citynote.repository;

import com.citynote.entity.CountyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CountyRepository extends JpaRepository<CountyEntity, Integer> {
    Optional<CountyEntity> findById(int id);

    Optional<CountyEntity> findByCountyNameEqualsIgnoreCase(String countyName);

    List<CountyEntity> findByCountyStateEqualsIgnoreCase(String state);
}
