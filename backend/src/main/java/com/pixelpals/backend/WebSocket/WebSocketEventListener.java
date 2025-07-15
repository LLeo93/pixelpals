package com.pixelpals.backend.WebSocket;

import com.pixelpals.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import java.security.Principal;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    private final UserService userService;

    public WebSocketEventListener(UserService userService) {
        this.userService = userService;
    }

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser(); // Ottieni il Principal dalla sessione STOMP

        if (principal != null) {
            String username = principal.getName();
            userService.getUserByUsername(username).ifPresent(user -> {
                userService.setUserOnlineStatus(user.getId(), true);
                logger.info("User {} (ID: {}) is now ONLINE", username, user.getId());
            });
        } else {
            logger.warn("DEBUG: Unauthenticated WebSocket CONNECT event detected.");
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser(); // Ottieni il Principal dalla sessione STOMP

        if (principal != null) {
            String username = principal.getName();
            userService.getUserByUsername(username).ifPresent(user -> {
                userService.setUserOnlineStatus(user.getId(), false);
                logger.info("User {} (ID: {}) is now OFFLINE", username, user.getId());
            });
        } else {
            logger.warn("DEBUG: Unauthenticated or already cleaned-up WebSocket DISCONNECT event detected.");
        }
    }
}
