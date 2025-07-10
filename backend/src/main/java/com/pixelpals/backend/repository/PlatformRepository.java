package com.pixelpals.backend.repository;
import com.pixelpals.backend.model.Platform;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
public interface PlatformRepository extends MongoRepository<Platform, String> {
    Optional<Platform> findByName(String name);

}




