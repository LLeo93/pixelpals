package com.pixelpals.backend.repository;
import com.pixelpals.backend.model.Badge;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BadgeRepository extends MongoRepository<Badge, String> {}
