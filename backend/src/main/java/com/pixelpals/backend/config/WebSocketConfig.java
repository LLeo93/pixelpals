package com.pixelpals.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Abilita il supporto per i messaggi basati su WebSocket
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Abilita un broker di messaggi in memoria per inviare messaggi ai client
        // I messaggi con prefisso "/topic" saranno indirizzati al broker
        config.enableSimpleBroker("/topic");
        // I messaggi con prefisso "/app" saranno indirizzati ai metodi @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registra un endpoint STOMP per la connessione WebSocket
        // I client si connetteranno a "/ws"
        // .withSockJS() abilita il fallback per i browser che non supportano i WebSocket nativi
        // .setAllowedOriginPatterns("*") permette connessioni da qualsiasi origine (per sviluppo)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Permette connessioni da qualsiasi origine (per sviluppo)
                .withSockJS();
    }
}
