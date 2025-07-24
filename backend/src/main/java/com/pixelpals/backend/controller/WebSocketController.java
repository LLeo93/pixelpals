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
    @MessageMapping("/chat.sendMessage/{matchId}")
    public void sendMessageToMatch(@DestinationVariable String matchId,
                                   @Payload MessageDTO messageDTO,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        String senderId = ((User) userDetails).getId();
        if (!senderId.equals(messageDTO.getSenderId())) {
            return;
        }
        messageDTO.setChatRoomId(matchId);
        MessageDTO savedMessage = messageService.sendMessage(messageDTO);
        messagingTemplate.convertAndSend("/topic/match/" + matchId + "/chat", savedMessage);
    }
    @MessageMapping("/chat.addUser")
    public void addUserToChat(@Payload MessageDTO messageDTO,
                              @AuthenticationPrincipal UserDetails userDetails) {
    }
}
