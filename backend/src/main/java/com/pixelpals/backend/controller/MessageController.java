package com.pixelpals.backend.controller;

import com.pixelpals.backend.dto.MessageDTO; // Importa MessageDTO
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.service.MessageService;
import com.pixelpals.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping; // Per WebSocket
import org.springframework.messaging.handler.annotation.Payload; // Per WebSocket
import org.springframework.messaging.simp.SimpMessageHeaderAccessor; // Per WebSocket
import org.springframework.messaging.simp.SimpMessagingTemplate; // Per WebSocket
import org.springframework.web.bind.annotation.*; // Per REST (fallback o history)

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages") // Prefisso per endpoint REST
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserService userService; // Per ottenere l'ID dell'utente dal principal
    private final SimpMessagingTemplate messagingTemplate; // Per inviare messaggi tramite WebSocket

    // Endpoint REST per recuperare la cronologia dei messaggi tra due utenti
    @GetMapping("/history/{user2Id}")
    public ResponseEntity<?> getChatHistory(@PathVariable String user2Id, Principal principal) {
        try {
            // Ottieni l'ID dell'utente corrente dal Principal
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

    // Endpoint WebSocket per inviare messaggi
    // I messaggi inviati a /app/chat.sendMessage verranno gestiti qui
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageDTO messageDTO, SimpMessageHeaderAccessor headerAccessor) {
        // Ottieni l'username del mittente dalla sessione WebSocket
        String senderUsername = headerAccessor.getUser().getName();
        User sender = userService.getUserByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Mittente WebSocket non trovato."));

        messageDTO.setSenderId(sender.getId());
        messageDTO.setSenderUsername(sender.getUsername());

        // Salva il messaggio nel database
        MessageDTO savedMessage = messageService.sendMessage(messageDTO);

        // Invia il messaggio al destinatario (se privato) o a una chat room
        // Per chat privata: /topic/messages/<receiverId>
        // Per chat di gruppo: /topic/messages/<chatRoomId>
        // Per ora, assumiamo chat privata o globale.
        if (savedMessage.getReceiverId() != null && !savedMessage.getReceiverId().isEmpty()) {
            // Invia al mittente
            messagingTemplate.convertAndSendToUser(
                    savedMessage.getSenderUsername(), "/queue/messages", savedMessage);
            // Invia al destinatario
            messagingTemplate.convertAndSendToUser(
                    savedMessage.getReceiverUsername(), "/queue/messages", savedMessage);
        } else {
            // Esempio: Invia a una chat globale se non c'è un destinatario specifico
            messagingTemplate.convertAndSend("/topic/public", savedMessage);
        }
    }

    // Endpoint WebSocket per aggiungere un utente alla chat (es. quando si connette)
    // I messaggi inviati a /app/chat.addUser verranno gestiti qui
    @MessageMapping("/chat.addUser")
    public void addUser(@Payload MessageDTO messageDTO, SimpMessageHeaderAccessor headerAccessor) {
        // Aggiungi l'username del nuovo utente alla sessione WebSocket
        headerAccessor.getSessionAttributes().put("username", messageDTO.getSenderUsername());

        // Puoi inviare un messaggio di benvenuto o notifica a tutti gli utenti connessi
        // Esempio: messagingTemplate.convertAndSend("/topic/public", messageDTO);
    }
}
