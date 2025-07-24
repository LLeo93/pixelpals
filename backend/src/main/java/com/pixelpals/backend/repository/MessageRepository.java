package com.pixelpals.backend.repository;
import com.pixelpals.backend.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;
import java.util.List;
@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByReceiverIdAndReadFalse(String receiverId);
    List<Message> findByChatRoomId(String chatRoomId, Sort sort);
    int countByReceiverIdAndReadFalse(String receiverId);
    List<Message> findByChatRoomIdAndReceiverIdAndReadFalse(String chatRoomId, String receiverId);
}
