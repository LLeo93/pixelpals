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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageDTO sendMessage(MessageDTO messageDTO) {
        User sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(() -> new RuntimeException("Mittente non trovato."));
        User receiver = userRepository.findById(messageDTO.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Destinatario non trovato."));

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(messageDTO.getContent());
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);
        message.setChatRoomId(messageDTO.getChatRoomId());

        Message savedMessage = messageRepository.save(message);

        Map<String, Integer> unreadCounts = getUnreadCountsPerChat(receiver.getId());
        String type = message.getChatRoomId().contains("_") ? "CHAT_FRIEND" : "CHAT_MATCH";

        messagingTemplate.convertAndSendToUser(
                receiver.getUsername(),
                "/queue/unread-updates",
                Map.of(
                        "type", type,
                        "chatRoomId", message.getChatRoomId(),
                        "unreadCount", unreadCounts.getOrDefault(message.getChatRoomId(), 1)
                )
        );

        return convertToDTO(savedMessage);
    }

    public void markMessagesAsRead(String userId, String chatRoomId) {
        List<Message> unreadMessages = messageRepository.findByChatRoomIdAndReceiverIdAndReadFalse(chatRoomId, userId);
        unreadMessages.forEach(msg -> msg.setRead(true));
        messageRepository.saveAll(unreadMessages);
    }

    public String generateChatRoomId(String user1Id, String user2Id) {
        List<String> userIds = Arrays.asList(user1Id, user2Id);
        userIds.sort(String::compareTo);
        return String.join("_", userIds);
    }

    public List<MessageDTO> getChatHistoryBetweenUsers(String user1Id, String user2Id) {
        String chatRoomId = generateChatRoomId(user1Id, user2Id);
        List<Message> messages = messageRepository.findByChatRoomId(chatRoomId, Sort.by(Sort.Direction.ASC, "timestamp"));
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MessageDTO> getChatHistoryForMatch(String matchId) {
        List<Message> messages = messageRepository.findByChatRoomId(matchId, Sort.by(Sort.Direction.ASC, "timestamp"));
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public int getTotalUnreadCount(String userId) {
        return messageRepository.countByReceiverIdAndReadFalse(userId);
    }

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
