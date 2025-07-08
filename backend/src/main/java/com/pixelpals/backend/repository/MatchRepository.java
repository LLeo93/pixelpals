package com.pixelpals.backend.repository;
import com.pixelpals.backend.model.Match;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MatchRepository extends MongoRepository<Match, String> {}

