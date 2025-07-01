package com.pixelpals.backend.repository;
import com.pixelpals.backend.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {}
