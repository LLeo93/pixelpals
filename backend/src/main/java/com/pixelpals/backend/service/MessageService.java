package com.pixelpals.backend.service;

import com.pixelpals.backend.dto.MessageDTO;
import com.pixelpals.backend.model.Message;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.repository.MessageRepository;
import com.pixelpals.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate; // Per inviare messaggi WebSocket

    // Metodo esistente per inviare messaggi
    public MessageDTO sendMessage(MessageDTO messageDTO) {
        User sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(() -> new RuntimeException("Mittente non trovato."));
        User receiver = userRepository.findById(messageDTO.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Destinatario non trovato."));

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(messageDTO.getContent());
        message.setTimestamp(LocalDateTime.now()); // Ora compatibile con Message.java
        message.setRead(false); // Nuovo messaggio è non letto
        message.setChatRoomId(messageDTO.getChatRoomId()); // Salva l'ID della chat room

        Message savedMessage = messageRepository.save(message);

        // Invia il messaggio al destinatario tramite WebSocket
        // Questo sarà gestito dal WebSocketController, ma il MessageService può inviare notifiche.
        // messagingTemplate.convertAndSendToUser(receiver.getUsername(), "/queue/messages", convertToDTO(savedMessage));

        return convertToDTO(savedMessage);
    }

    // Metodo esistente per marcare i messaggi come letti
    // Questo metodo ora accetta l'ID dell'utente e l'ID della chat room
    public void markMessagesAsRead(String userId, String chatRoomId) {
        // Trova tutti i messaggi non letti in una specifica chatRoomId destinati a userId
        List<Message> unreadMessages = messageRepository.findByChatRoomIdAndReceiverIdAndReadFalse(chatRoomId, userId);
        unreadMessages.forEach(msg -> msg.setRead(true));
        messageRepository.saveAll(unreadMessages);
    }

    // Metodo esistente per generare l'ID della chat room
    public String generateChatRoomId(String user1Id, String user2Id) {
        // Ordina gli ID per garantire un ID di chat room consistente
        List<String> userIds = Arrays.asList(user1Id, user2Id);
        userIds.sort(String::compareTo);
        return String.join("_", userIds);
    }

    // Metodo per ottenere la cronologia della chat tra due utenti (vecchio metodo, ora usa chatRoomId)
    // Rinomino per chiarezza e per allinearlo con il controller
    public List<MessageDTO> getChatHistoryBetweenUsers(String user1Id, String user2Id) {
        String chatRoomId = generateChatRoomId(user1Id, user2Id);
        List<Message> messages = messageRepository.findByChatRoomId(chatRoomId, Sort.by(Sort.Direction.ASC, "timestamp"));
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Recupera la cronologia della chat per un match specifico.
     * Il chatRoomId è l'ID del match stesso.
     * @param matchId L'ID del match (che funge da chatRoomId).
     * @return Lista di MessageDTO.
     */
    public List<MessageDTO> getChatHistoryForMatch(String matchId) {
        List<Message> messages = messageRepository.findByChatRoomId(matchId, Sort.by(Sort.Direction.ASC, "timestamp"));
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * NUOVO: Ottiene il conteggio totale dei messaggi non letti per un utente.
     * @param userId L'ID dell'utente.
     * @return Il conteggio totale dei messaggi non letti.
     */
    public int getTotalUnreadCount(String userId) {
        return messageRepository.countByReceiverIdAndReadFalse(userId);
    }

    /**
     * NUOVO: Ottiene i conteggi dei messaggi non letti per ogni chat room per un utente.
     * @param userId L'ID dell'utente.
     * @return Una mappa dove la chiave è l'ID della chat room e il valore è il conteggio dei messaggi non letti.
     */
    public Map<String, Integer> getUnreadCountsPerChat(String userId) {
        List<Message> unreadMessages = messageRepository.findByReceiverIdAndReadFalse(userId);
        return unreadMessages.stream()
                .collect(Collectors.groupingBy(Message::getChatRoomId, Collectors.summingInt(msg -> 1)));
    }

    private MessageDTO convertToDTO(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .receiverId(message.getReceiver().getId())
                .receiverUsername(message.getReceiver().getUsername())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .read(message.isRead())
                .chatRoomId(message.getChatRoomId())
                .build();
    }
}
