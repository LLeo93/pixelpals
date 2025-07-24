package com.pixelpals.backend.repository;
import com.pixelpals.backend.model.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends MongoRepository<Game, String> {
    Optional<Game> findByName(String name);
    List<Game> findByNameContainingIgnoreCase(String name);
}
