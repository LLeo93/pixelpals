package com.pixelpals.backend.WebSocket;
import com.pixelpals.backend.service.JwtService;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class WebSocketAuthenticationInterceptor implements ChannelInterceptor {

    private final JwtService jwtService; // Usiamo JwtService come nel tuo JwtAuthFilter
    private final UserDetailsService userDetailsService;

    public WebSocketAuthenticationInterceptor(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // Rimuovi "Bearer "

                try {
                    if (jwtService.isTokenValid(token, userDetailsService.loadUserByUsername(jwtService.extractUsername(token)))) {
                        String username = jwtService.extractUsername(token);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        accessor.setUser(authentication);
                        // Opzionale: Imposta nel SecurityContextHolder anche per il thread corrente, se necessario per altri filtri/logiche
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("DEBUG: WebSocketAuthenticationInterceptor - Utente autenticato per sessione STOMP: " + username);

                        // Potresti anche voler salvare l'username nella sessione STOMP se ti serve altrove
                        accessor.getSessionAttributes().put("username", username);

                    } else {
                        System.out.println("DEBUG: WebSocketAuthenticationInterceptor - Token JWT WebSocket non valido o scaduto.");
                        // Non chiudiamo esplicitamente qui, l'errore potrebbe essere gestito a un livello superiore o la connessione semplicemente non sarà autenticata.
                    }
                } catch (Exception e) {
                    System.err.println("DEBUG: WebSocketAuthenticationInterceptor - Errore durante l'autenticazione JWT per WebSocket: " + e.getMessage());
                    // Considera di chiudere la connessione STOMP o di inviare un messaggio di errore al client se l'autenticazione fallisce
                }
            } else {
                System.out.println("DEBUG: WebSocketAuthenticationInterceptor - Header Authorization non trovato o non nel formato Bearer per WebSocket CONNECT.");
            }
        }
        return message;
    }
}