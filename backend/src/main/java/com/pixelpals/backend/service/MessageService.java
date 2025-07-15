package com.pixelpals.backend.service;

import com.pixelpals.backend.dto.MessageDTO;
import com.pixelpals.backend.model.Message;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.repository.MessageRepository;
import com.pixelpals.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate; // Importa MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria; // Importa Criteria
import org.springframework.data.mongodb.core.query.Query; // Importa Query
import org.springframework.data.mongodb.core.query.Update; // Importa Update
import org.springframework.messaging.simp.SimpMessagingTemplate; // Per inviare notifiche WebSocket
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate; // Iniettato per operazioni di aggiornamento più complesse
    private final SimpMessagingTemplate messagingTemplate; // Per inviare notifiche WebSocket

    // Metodo per inviare e salvare un messaggio
    public MessageDTO sendMessage(MessageDTO messageDTO) {
        User sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(() -> new RuntimeException("Mittente non trovato con ID: " + messageDTO.getSenderId()));

        User receiver = null;
        if (messageDTO.getReceiverId() != null && !messageDTO.getReceiverId().isEmpty()) {
            receiver = userRepository.findById(messageDTO.getReceiverId())
                    .orElseThrow(() -> new RuntimeException("Destinatario non trovato con ID: " + messageDTO.getReceiverId()));
        }

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(messageDTO.getContent());
        message.setTimestamp(new Date());
        message.setRead(false); // NUOVO: Imposta il messaggio come non letto di default

        if (sender != null && receiver != null) {
            message.setChatRoomId(generateChatRoomId(sender.getId(), receiver.getId()));
        } else {
            message.setChatRoomId("global_chat");
        }

        Message savedMessage = messageRepository.save(message);

        // NUOVO: Aggiorna il conteggio dei messaggi non letti per il destinatario
        if (receiver != null) {
            updateUnreadCount(receiver.getId(), savedMessage.getChatRoomId(), 1); // Incrementa di 1
            // Invia una notifica WebSocket al destinatario con il nuovo conteggio
            sendUnreadCountUpdate(receiver.getUsername(), savedMessage.getChatRoomId());
        }

        // Converte l'entità salvata in DTO per la risposta
        messageDTO.setId(savedMessage.getId());
        messageDTO.setTimestamp(savedMessage.getTimestamp());
        messageDTO.setSenderUsername(savedMessage.getSender().getUsername());
        if (savedMessage.getReceiver() != null) {
            messageDTO.setReceiverUsername(savedMessage.getReceiver().getUsername());
        }
        messageDTO.setRead(savedMessage.isRead()); // NUOVO: Imposta il flag read nel DTO
        return messageDTO;
    }

    // Recupera la cronologia di una chat privata tra due utenti
    public List<MessageDTO> getChatHistory(String user1Id, String user2Id) {
        String chatRoomId = generateChatRoomId(user1Id, user2Id);
        List<Message> messages = messageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoomId);

        // Converte le entità Message in MessageDTO
        return messages.stream().map(msg -> {
            MessageDTO dto = new MessageDTO();
            dto.setId(msg.getId());
            dto.setSenderId(msg.getSender().getId());
            dto.setSenderUsername(msg.getSender().getUsername());
            if (msg.getReceiver() != null) {
                dto.setReceiverId(msg.getReceiver().getId());
                dto.setReceiverUsername(msg.getReceiver().getUsername());
            }
            dto.setContent(msg.getContent());
            dto.setTimestamp(msg.getTimestamp());
            dto.setChatRoomId(msg.getChatRoomId());
            dto.setRead(msg.isRead()); // NUOVO: Imposta il flag read nel DTO
            return dto;
        }).collect(Collectors.toList());
    }

    // NUOVO: Metodo per marcare i messaggi di una chat come letti
    public void markChatAsRead(String userId, String chatRoomId) {
        // Trova tutti i messaggi non letti in questa chatRoomId dove l'utente è il destinatario
        Query query = new Query(
                Criteria.where("chatRoomId").is(chatRoomId)
                        .and("receiver.$id").is(userId) // Assumi che DBRef salvi l'ID come $id
                        .and("read").is(false)
        );
        Update update = new Update().set("read", true);
        mongoTemplate.updateMulti(query, update, Message.class);

        // Resetta il conteggio non letto per questa chatroom per l'utente
        updateUnreadCount(userId, chatRoomId, 0); // Imposta a 0
        // Invia una notifica WebSocket all'utente con il conteggio aggiornato
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + userId));
        sendUnreadCountUpdate(user.getUsername(), chatRoomId);
    }

    // NUOVO: Metodo per ottenere il conteggio totale dei messaggi non letti per un utente
    public int getTotalUnreadCount(String userId) {
        // Recupera la mappa dei conteggi non letti per l'utente
        Map<String, Integer> unreadCounts = getUnreadCountsPerChat(userId);
        return unreadCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    // NUOVO: Metodo per ottenere i conteggi non letti per ogni chatroom per un utente
    // Useremo una collezione separata 'unread_counts' per questo, per efficienza
    public Map<String, Integer> getUnreadCountsPerChat(String userId) {
        // Cerca il documento che contiene i conteggi non letti per questo utente
        // Assumi una collezione "unread_counts" con documenti del tipo:
        // { "_id": "userId", "counts": { "chatRoomId1": 5, "chatRoomId2": 2 } }
        Query query = new Query(Criteria.where("_id").is(userId));
        Map result = mongoTemplate.findOne(query, Map.class, "unread_counts");

        if (result != null && result.containsKey("counts")) {
            return (Map<String, Integer>) result.get("counts");
        }
        return new HashMap<>(); // Ritorna una mappa vuota se non ci sono conteggi
    }

    // NUOVO: Metodo interno per aggiornare il conteggio non letto in MongoDB
    private void updateUnreadCount(String userId, String chatRoomId, int change) {
        Query query = new Query(Criteria.where("_id").is(userId));
        Update update = new Update();

        if (change == 0) { // Se il cambiamento è 0, significa che stiamo resettando il conteggio
            update.unset("counts." + chatRoomId); // Rimuove il campo per quella chatroom
        } else { // Altrimenti, incrementa o imposta
            update.inc("counts." + chatRoomId, change); // Incrementa il conteggio per la chatroom
        }

        // upsert: crea il documento se non esiste
        mongoTemplate.upsert(query, update, "unread_counts");
    }

    // NUOVO: Metodo per inviare aggiornamenti dei conteggi non letti via WebSocket
    private void sendUnreadCountUpdate(String username, String chatRoomId) {
        // Recupera i nuovi conteggi per l'utente
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato: " + username));
        String userId = user.getId();

        int totalUnread = getTotalUnreadCount(userId);
        Map<String, Integer> unreadPerChat = getUnreadCountsPerChat(userId);

        // Crea un oggetto di aggiornamento da inviare al frontend
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("totalUnreadCount", totalUnread);
        updatePayload.put("chatRoomId", chatRoomId); // Invia l'ID della chatroom aggiornata
        updatePayload.put("unreadCount", unreadPerChat.getOrDefault(chatRoomId, 0)); // Invia il conteggio specifico

        // Invia l'aggiornamento alla coda privata dell'utente
        messagingTemplate.convertAndSendToUser(username, "/queue/unread-updates", updatePayload);
        System.out.println("DEBUG: Inviato aggiornamento non letto a " + username + ": " + updatePayload);
    }

    // Metodo helper per generare un ID di chat room consistente per chat private
    public String generateChatRoomId(String user1Id, String user2Id) {
        return user1Id.compareTo(user2Id) < 0 ? user1Id + "_" + user2Id : user2Id + "_" + user1Id;
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + userId));
    }
}
