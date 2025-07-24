package com.pixelpals.backend.WebSocket;

import com.pixelpals.backend.dto.UserStatusDTO;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.service.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
public class WebSocketEventListener {

    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserSessionRegistry userSessionRegistry;

    public WebSocketEventListener(UserService userService, SimpMessagingTemplate messagingTemplate, UserSessionRegistry userSessionRegistry) {
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
        this.userSessionRegistry = userSessionRegistry;
    }

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        String sessionId = accessor.getSessionId();

        if (principal != null) {
            String username = principal.getName();
            userService.getUserByUsername(username).ifPresent(user -> {
                boolean wasOffline = userSessionRegistry.registerSession(user.getId(), sessionId);
                if (!user.isOnline()) {
                    userService.setUserOnlineStatus(user.getId(), true);
                }
                messagingTemplate.convertAndSend("/topic/status",
                        new UserStatusDTO(user.getId(), user.getUsername(), true));
            });
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        String sessionId = accessor.getSessionId();

        if (principal != null) {
            String username = principal.getName();
            userService.getUserByUsername(username).ifPresent(user -> {
                boolean isNowOffline = userSessionRegistry.deregisterSession(user.getId(), sessionId);
                if (isNowOffline) {
                    userService.setUserOnlineStatus(user.getId(), false);
                    messagingTemplate.convertAndSend("/topic/status",
                            new UserStatusDTO(user.getId(), user.getUsername(), false));
                }
            });
        }
    }
}
