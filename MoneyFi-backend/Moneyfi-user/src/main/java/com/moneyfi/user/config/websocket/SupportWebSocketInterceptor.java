package com.moneyfi.user.config.websocket;

import com.moneyfi.user.model.secondary.SupportConversation;
import com.moneyfi.user.repository.secondary.SupportConversationRepository;
import com.moneyfi.user.service.primary.jwtservice.impl.JwtServiceImplementation;
import com.moneyfi.user.service.primary.jwtservice.impl.MyUserDetailsService;
import com.moneyfi.user.service.primary.user.CommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SupportWebSocketInterceptor implements ChannelInterceptor {

    private static final String SUPPORT_TOPIC_PREFIX = "/topic/support/";

    private final JwtServiceImplementation jwtService;
    private final ApplicationContext context;
    private final CommonService commonService;
    private final SupportConversationRepository conversationRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // 1. Authenticate the WebSocket connection
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new MessageDeliveryException(
                        "Missing WebSocket authentication token");
            }

            String token = authHeader.substring(7);

            String username = jwtService.extractUserName(token);

            if (username == null ||
                    commonService.isTokenBlacklisted(token, username)) {
                throw new MessageDeliveryException(
                        "Invalid or blacklisted token");
            }

            MyUserDetailsService userDetailsService =
                    context.getBean(MyUserDetailsService.class);

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            if (!jwtService.validateToken(token, userDetails)) {
                throw new MessageDeliveryException(
                        "Invalid or expired token");
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            accessor.setUser(authentication);
        }

        // 2. Authorize conversation subscriptions
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            String destination = accessor.getDestination();

            if (destination != null &&
                    destination.startsWith(SUPPORT_TOPIC_PREFIX)) {

                String conversationId =
                        destination.substring(SUPPORT_TOPIC_PREFIX.length());

                // Reject empty IDs or additional path segments
                if (conversationId.isBlank() ||
                        conversationId.contains("/")) {
                    throw new MessageDeliveryException(
                            "Invalid conversation destination");
                }

                if (accessor.getUser() == null) {
                    throw new MessageDeliveryException(
                            "Unauthenticated WebSocket connection");
                }

                String username = accessor.getUser().getName();

                SupportConversation conversation =
                        conversationRepository.findById(conversationId)
                                .orElseThrow(() ->
                                        new MessageDeliveryException(
                                                "Conversation not found"));

                boolean authorized =
                        username.equals(conversation.getUserId()) ||
                                username.equals(conversation.getAgentId());

                if (!authorized) {
                    throw new MessageDeliveryException(
                            "You are not authorized to access this conversation");
                }
            }
        }

        return message;
    }
}
