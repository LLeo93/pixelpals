package com.pixelpals.backend.config;
import com.pixelpals.backend.WebSocket.WebSocketAuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.context.annotation.Bean;
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final WebSocketAuthenticationInterceptor webSocketAuthenticationInterceptor;
    public WebSocketConfig(WebSocketAuthenticationInterceptor webSocketAuthenticationInterceptor) {
        this.webSocketAuthenticationInterceptor = webSocketAuthenticationInterceptor;
    }

    @Bean
    public TaskScheduler heartBeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("wss-heartbeat-scheduler-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/user")
                .setHeartbeatValue(new long[]{120000, 120000})
                .setTaskScheduler(heartBeatScheduler());
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setSessionCookieNeeded(false)
                .setDisconnectDelay(30 * 1000)
                .setHeartbeatTime(120 * 1000);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthenticationInterceptor);
    }
}

