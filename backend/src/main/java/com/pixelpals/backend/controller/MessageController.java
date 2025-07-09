package com.pixelpals.backend.controller;

import com.pixelpals.backend.dto.MessageDTO;
import com.pixelpals.backend.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    // REST fallback
    @GetMapping("/{matchId}")
    public List<MessageDTO> getMessages(@PathVariable String matchId) {
        return messageService.getMessagesByMatch(matchId);
    }

    @PostMapping
    public MessageDTO sendMessage(@RequestBody MessageDTO dto) {
        return messageService.sendMessage(dto);
    }

    // WebSocket endpoint
    @MessageMapping("/chat") // client -> /app/chat
    @SendTo("/topic/messages") // broadcast
    public MessageDTO handleWebSocketMessage(MessageDTO dto) {
        return messageService.sendMessage(dto);
    }
}
