package com.example.user.repository;

import com.example.user.entity.Tokens;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TokenRepository extends MongoRepository<Tokens, String> {

}
