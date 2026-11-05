package com.moneyfi.user.config.websocket;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class SupportWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final SupportWebSocketInterceptor supportWebSocketInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Client sends messages to destinations starting with /app
        registry.setApplicationDestinationPrefixes("/app"); //client to server messages

        // Server broadcasts messages to subscribers
        registry.enableSimpleBroker("/topic", "/queue"); // topic -> server to client broadcast, queue-> user specific messages

        // Prefix for user-specific destinations
        registry.setUserDestinationPrefix("/user"); // user destination prefix
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/support") // websocket connection end point
                .setAllowedOriginPatterns(
                        "http://localhost:4200",
                        "http://localhost:4300"
                );
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(supportWebSocketInterceptor);
    }
}

