package com.pixelpals.backend.repository;

import com.pixelpals.backend.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    // Trova tutti i messaggi per una specifica chat room, ordinati per timestamp
    List<Message> findByChatRoomIdOrderByTimestampAsc(String chatRoomId);
}
