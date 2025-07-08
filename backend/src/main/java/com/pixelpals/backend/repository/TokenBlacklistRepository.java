package com.pixelpals.backend.repository;

import com.pixelpals.backend.model.BlacklistedToken;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TokenBlacklistRepository extends MongoRepository<BlacklistedToken, String> {
    boolean existsById(String token);
}
