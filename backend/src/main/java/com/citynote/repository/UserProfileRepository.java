package com.citynote.repository;

import com.citynote.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
    
    @Query("SELECT up FROM UserProfile up WHERE up.user.username = :username")
    Optional<UserProfile> findByUsername(@Param("username") String username);
}
