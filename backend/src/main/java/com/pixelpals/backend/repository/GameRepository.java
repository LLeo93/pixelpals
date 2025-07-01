package com.pixelpals.backend.repository;
import com.pixelpals.backend.model.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GameRepository extends MongoRepository<Game, String> {}
