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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date; // Assicurati di avere questo import

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

            List<MessageDTO> messages = messageService.getChatHistory(user1Id, user2Id);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Errore nel recupero della cronologia chat: " + e.getMessage()));
        }
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageDTO messageDTO, Principal principal) {
        String senderUsername = principal.getName();
        User sender = userService.getUserByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Mittente WebSocket non trovato."));

        messageDTO.setSenderId(sender.getId());
        messageDTO.setSenderUsername(sender.getUsername());
        messageDTO.setTimestamp(Date.from(java.time.Instant.now()));

        MessageDTO savedMessage = messageService.sendMessage(messageDTO);
        System.out.println("Messaggio ricevuto e salvato: " + savedMessage.getContent() + " da " + savedMessage.getSenderUsername());

        String chatRoomTopic = "/topic/chatRoom/" + savedMessage.getChatRoomId();
        messagingTemplate.convertAndSend(chatRoomTopic, savedMessage);
        System.out.println("Messaggio inoltrato al topic: " + chatRoomTopic);

        // Non è più necessario inviare alla coda privata qui, poiché il MessageService
        // si occupa già di inviare l'aggiornamento del conteggio non letto al destinatario.
        // Se si vuole anche il messaggio completo sulla coda privata, allora si può tenere.
        // if (savedMessage.getReceiverUsername() != null && !savedMessage.getReceiverUsername().isEmpty()) {
        //     messagingTemplate.convertAndSendToUser(
        //            savedMessage.getReceiverUsername(), "/queue/messages", savedMessage);
        // }
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload MessageDTO messageDTO, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", messageDTO.getSenderUsername());
        System.out.println("DEBUG: Utente " + messageDTO.getSenderUsername() + " aggiunto alla sessione WebSocket.");
    }

    // NUOVO: Endpoint REST per ottenere il conteggio totale dei messaggi non letti
    @GetMapping("/unread/total")
    public ResponseEntity<Integer> getTotalUnreadCount(Principal principal) {
        try {
            User currentUser = userService.getUserByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Utente corrente non trovato."));
            int total = messageService.getTotalUnreadCount(currentUser.getId());
            return ResponseEntity.ok(total);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0); // O un codice di errore più specifico
        }
    }

    // NUOVO: Endpoint REST per ottenere i conteggi non letti per ogni chatroom
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

    // NUOVO: Endpoint REST per marcare i messaggi di una chat come letti
    @PostMapping("/mark-read/{chatRoomId}")
    public ResponseEntity<Void> markChatAsRead(@PathVariable String chatRoomId, Principal principal) {
        try {
            User currentUser = userService.getUserByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Utente corrente non trovato."));
            messageService.markChatAsRead(currentUser.getId(), chatRoomId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
