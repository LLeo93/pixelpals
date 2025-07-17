package com.pixelpals.backend.repository;

import com.pixelpals.backend.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort; // Importa Sort

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    // Metodo per trovare messaggi non letti tra due utenti specifici (non più usato direttamente dal controller per marcare come letto)
    // List<Message> findBySenderIdAndReceiverIdAndReadFalse(String senderId, String receiverId); // Rimosso o modificato

    // Metodo per trovare tutti i messaggi non letti ricevuti da un utente
    List<Message> findByReceiverIdAndReadFalse(String receiverId);

    // Metodo per trovare la cronologia della chat per un dato chatRoomId, ordinata per timestamp
    List<Message> findByChatRoomId(String chatRoomId, Sort sort);

    // NUOVO: Metodo per contare i messaggi non letti per un utente specifico
    int countByReceiverIdAndReadFalse(String receiverId);

    // NUOVO: Metodo per trovare messaggi non letti in una chat room specifica per un utente
    List<Message> findByChatRoomIdAndReceiverIdAndReadFalse(String chatRoomId, String receiverId);
}
