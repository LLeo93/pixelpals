package com.pixelpals.backend.repository;
import com.pixelpals.backend.model.Badge;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface BadgeRepository extends MongoRepository<Badge, String> {
    Optional<Badge> findByName(String name);
}
