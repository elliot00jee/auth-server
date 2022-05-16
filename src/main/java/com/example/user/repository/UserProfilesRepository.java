package com.example.user.repository;

import com.example.user.entity.UserProfiles;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserProfilesRepository extends MongoRepository<UserProfiles, String> {
    Optional<UserProfiles> findByUserId(String userId);
}
