package com.pixelpals.backend.service;

import com.pixelpals.backend.dto.MessageDTO; // Importa MessageDTO
import com.pixelpals.backend.model.Message;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.repository.MessageRepository;
import com.pixelpals.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

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
        message.setReceiver(receiver); // Può essere null per chat di gruppo o broadcast
        message.setContent(messageDTO.getContent());
        message.setTimestamp(new Date()); // Imposta il timestamp al momento del salvataggio

        // Genera l'ID della chat room per chat private tra due utenti
        if (sender != null && receiver != null) {
            message.setChatRoomId(generateChatRoomId(sender.getId(), receiver.getId()));
        } else {
            // Per messaggi non privati, potresti avere un ID di chat room fisso o logica diversa
            message.setChatRoomId("global_chat"); // Esempio per una chat globale
        }

        Message savedMessage = messageRepository.save(message);

        // Converte l'entità salvata in DTO per la risposta
        messageDTO.setId(savedMessage.getId());
        messageDTO.setTimestamp(savedMessage.getTimestamp());
        messageDTO.setSenderUsername(savedMessage.getSender().getUsername());
        if (savedMessage.getReceiver() != null) {
            messageDTO.setReceiverUsername(savedMessage.getReceiver().getUsername());
        }
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
            return dto;
        }).collect(Collectors.toList());
    }

    // Metodo helper per generare un ID di chat room consistente per chat private
    public String generateChatRoomId(String user1Id, String user2Id) {
        // Assicura che l'ID sia sempre lo stesso indipendentemente dall'ordine degli utenti
        // Ordina alfabeticamente gli ID per garantire consistenza
        return user1Id.compareTo(user2Id) < 0 ? user1Id + "_" + user2Id : user2Id + "_" + user1Id;
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + userId));
    }
}
