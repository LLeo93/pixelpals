package com.pixelpals.backend.repository;

import com.pixelpals.backend.entity.RefreshTokenEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshTokenEntity, String> {
    Optional<RefreshTokenEntity> findByToken(String token);
    void deleteByUsername(String username);
}
