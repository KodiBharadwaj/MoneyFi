package com.moneyfi.user.config.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import static com.moneyfi.constants.constants.BasePathConstants.USER_SERVICE_BASE_PATH;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class SupportWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    private final SupportWebSocketInterceptor supportWebSocketInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes(USER_SERVICE_BASE_PATH + "/app");
        registry.enableSimpleBroker(USER_SERVICE_BASE_PATH + "/topic", USER_SERVICE_BASE_PATH + "/queue");
        registry.setUserDestinationPrefix(USER_SERVICE_BASE_PATH + "/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(USER_SERVICE_BASE_PATH + "/ws/support")
                .setAllowedOriginPatterns(allowedOrigins.split("\\s*,\\s*"));
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(supportWebSocketInterceptor);
    }
}
