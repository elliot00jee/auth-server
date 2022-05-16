package com.example.user.repository;


import com.example.user.entity.UserAuth;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserAuthRepository extends MongoRepository<UserAuth, String> {
    Optional<UserAuth> findByUserId(String userId);
}
