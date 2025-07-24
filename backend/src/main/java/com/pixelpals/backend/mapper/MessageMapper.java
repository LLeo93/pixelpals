package com.pixelpals.backend.mapper;
import com.pixelpals.backend.dto.MessageDTO;
import com.pixelpals.backend.model.Message;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
@Component
public class MessageMapper {
    public MessageDTO toDTO(Message message) {
        if (message == null) {
            return null;
        }
        return MessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSender() != null ? message.getSender().getId() : null)
                .senderUsername(message.getSender() != null ? message.getSender().getUsername() : null)
                .receiverId(message.getReceiver() != null ? message.getReceiver().getId() : null)
                .receiverUsername(message.getReceiver() != null ? message.getReceiver().getUsername() : null)
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .read(message.isRead())
                .chatRoomId(message.getChatRoomId())
                .build();
    }
    public Message toEntity(MessageDTO messageDTO) {
        if (messageDTO == null) {
            return null;
        }
        Message message = new Message();
        message.setId(messageDTO.getId());
        message.setContent(messageDTO.getContent());
        message.setTimestamp(messageDTO.getTimestamp());
        message.setRead(messageDTO.isRead());
        message.setChatRoomId(messageDTO.getChatRoomId());
        return message;
    }
}