package com.pixelpals.backend.mapper;

import com.pixelpals.backend.dto.MessageDTO;
import com.pixelpals.backend.model.Message;
import com.pixelpals.backend.model.User; // Import User if not already present

public class MessageMapper {

    public static MessageDTO toDTO(Message message) {
        if (message == null) {
            return null;
        }
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setChatRoomId(message.getChatRoomId());

        if (message.getSender() != null) {
            dto.setSenderId(message.getSender().getId());
            dto.setSenderUsername(message.getSender().getUsername());
        }
        if (message.getReceiver() != null) {
            dto.setReceiverId(message.getReceiver().getId());
            dto.setReceiverUsername(message.getReceiver().getUsername());
        }
        return dto;
    }

    // Questo metodo è meno comune da usare per la chat in tempo reale (dove si riceve un DTO e si salva l'entità),
    // ma può essere utile per altri scopi.
    // Nota: per convertire da DTO a Entity, avresti bisogno di recuperare gli oggetti User completi dal repository.
    // Per ora, lo lascio come scheletro o lo ometto se non strettamente necessario per evitare dipendenze circolari.
    /*
    public static Message toEntity(MessageDTO dto, UserRepository userRepository) {
        if (dto == null) {
            return null;
        }
        Message message = new Message();
        message.setId(dto.getId());
        message.setContent(dto.getContent());
        message.setTimestamp(dto.getTimestamp());
        message.setChatRoomId(dto.getChatRoomId());

        // Recupera il mittente e il destinatario dal repository
        if (dto.getSenderId() != null) {
            User sender = userRepository.findById(dto.getSenderId())
                                        .orElseThrow(() -> new RuntimeException("Sender not found"));
            message.setSender(sender);
        }
        if (dto.getReceiverId() != null) {
            User receiver = userRepository.findById(dto.getReceiverId())
                                          .orElseThrow(() -> new RuntimeException("Receiver not found"));
            message.setReceiver(receiver);
        }
        return message;
    }
    */
}
