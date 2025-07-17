package com.pixelpals.backend.controller;
import com.pixelpals.backend.dto.MessageDTO;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    @MessageMapping("/chat.sendMessage/{matchId}") // Frontend invia a /app/chat.sendMessage/{matchId}
    public void sendMessageToMatch(@DestinationVariable String matchId,
                                   @Payload MessageDTO messageDTO,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        // Assicurati che il senderId nel DTO corrisponda all'utente autenticato
        String senderId = ((User) userDetails).getId();
        if (!senderId.equals(messageDTO.getSenderId())) {
            // Logga o gestisci l'errore: tentativo di inviare un messaggio per conto di un altro utente
            System.err.println("Tentativo di inviare messaggio con senderId non corrispondente all'utente autenticato.");
            return;
        }

        // Imposta l'ID della chat room con l'ID del match
        messageDTO.setChatRoomId(matchId);

        // Salva il messaggio nel database
        MessageDTO savedMessage = messageService.sendMessage(messageDTO);

        // Invia il messaggio a tutti i sottoscrittori del topic della chat del match
        // Frontend si sottoscrive a /topic/match/{matchId}/chat
        messagingTemplate.convertAndSend("/topic/match/" + matchId + "/chat", savedMessage);

        // Opzionale: Invia una notifica di messaggio non letto al destinatario specifico
        // Questo potrebbe essere gestito anche dal MessageService o da un servizio di notifica più generale
        // Se il tuo MessageService già invia notifiche per messaggi non letti, potresti non aver bisogno di questo qui.
        // Esempio:
        // messagingTemplate.convertAndSendToUser(savedMessage.getReceiverUsername(), "/queue/messages", savedMessage);
    }

    @MessageMapping("/chat.addUser") // Frontend invia a /app/chat.addUser
    public void addUserToChat(@Payload MessageDTO messageDTO,
                              @AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("Utente " + userDetails.getUsername() + " si è unito alla chat.");

    }
}
