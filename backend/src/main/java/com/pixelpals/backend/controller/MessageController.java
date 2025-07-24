package com.pixelpals.backend.controller;
import com.pixelpals.backend.dto.MessageDTO;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.service.MessageService;
import com.pixelpals.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/history/{user2Id}")
    public ResponseEntity<?> getChatHistory(@PathVariable String user2Id, Principal principal) {
        try {
            User currentUser = userService.getUserByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Utente corrente non trovato."));
            String user1Id = currentUser.getId();

            List<MessageDTO> messages = messageService.getChatHistoryBetweenUsers(user1Id, user2Id);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Errore nel recupero della cronologia chat: " + e.getMessage()));
        }
    }

    @GetMapping("/match/{matchId}/history")
    public ResponseEntity<?> getMatchChatHistory(@PathVariable String matchId, Principal principal) {
        try {
            List<MessageDTO> messages = messageService.getChatHistoryForMatch(matchId);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Errore nel recupero della cronologia chat del match: " + e.getMessage()));
        }
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageDTO messageDTO, Principal principal) {
        String senderUsername = principal.getName();
        User sender = userService.getUserByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Mittente WebSocket non trovato."));
        messageDTO.setSenderId(sender.getId());
        messageDTO.setSenderUsername(sender.getUsername());
        messageDTO.setTimestamp(LocalDateTime.now());
        MessageDTO savedMessage = messageService.sendMessage(messageDTO);
        String chatRoomTopic = "/topic/chatRoom/" + savedMessage.getChatRoomId();
        messagingTemplate.convertAndSend(chatRoomTopic, savedMessage);

        if (savedMessage.getReceiverId() != null) {
            int totalUnread = messageService.getTotalUnreadCount(savedMessage.getReceiverId());
            Map<String, Integer> unreadCountsPerChat = messageService.getUnreadCountsPerChat(savedMessage.getReceiverId());
            Map<String, Object> updatePayload = new HashMap<>();
            updatePayload.put("totalUnreadCount", totalUnread);
            updatePayload.put("unreadCountsPerChat", unreadCountsPerChat);
            updatePayload.put("chatRoomId", savedMessage.getChatRoomId());
            messagingTemplate.convertAndSendToUser(
                    userService.getUserById(savedMessage.getReceiverId()).orElseThrow().getUsername(),
                    "/queue/unread-updates",
                    updatePayload
            );
        }
    }

    @GetMapping("/unread/total")
    public ResponseEntity<Integer> getTotalUnreadCount(Principal principal) {
        try {
            User currentUser = userService.getUserByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Utente corrente non trovato."));
            int total = messageService.getTotalUnreadCount(currentUser.getId());
            return ResponseEntity.ok(total);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0);
        }
    }

    @GetMapping("/unread/per-chat")
    public ResponseEntity<Map<String, Integer>> getUnreadCountsPerChat(Principal principal) {
        try {
            User currentUser = userService.getUserByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Utente corrente non trovato."));
            Map<String, Integer> counts = messageService.getUnreadCountsPerChat(currentUser.getId());
            return ResponseEntity.ok(counts);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<>());
        }
    }

    @PostMapping("/mark-read/{chatRoomId}")
    public ResponseEntity<Void> markChatAsRead(@PathVariable String chatRoomId, Principal principal) {
        try {
            User currentUser = userService.getUserByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Utente corrente non trovato."));
            messageService.markMessagesAsRead(currentUser.getId(), chatRoomId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
