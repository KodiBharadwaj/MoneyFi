package com.moneyfi.user.config.websocket;

import com.moneyfi.user.model.secondary.SupportConversation;
import com.moneyfi.user.repository.secondary.SupportConversationRepository;
import com.moneyfi.user.service.primary.jwtservice.impl.JwtServiceImplementation;
import com.moneyfi.user.service.primary.jwtservice.impl.MyUserDetailsService;
import com.moneyfi.user.service.primary.user.CommonService;
import com.moneyfi.user.service.primary.user.UserAuthService;
import com.moneyfi.user.util.constants.StringConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.nio.file.AccessDeniedException;
import java.security.Principal;

import static com.moneyfi.constants.constants.BasePathConstants.USER_SERVICE_BASE_PATH;
import static com.moneyfi.constants.constants.CommonConstants.*;
import static com.moneyfi.user.util.StringUrls.TOPIC_SUPPORT_URL;
import static com.moneyfi.user.util.constants.StringConstants.CONVERSATION_NOT_FOUND;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupportWebSocketInterceptor implements ChannelInterceptor {

    private final JwtServiceImplementation jwtService;
    private final ApplicationContext applicationContext;
    private final CommonService commonService;
    private final SupportConversationRepository conversationRepository;
    private final UserAuthService userAuthService;

    private static final String SUPPORT_TOPIC_PREFIX = USER_SERVICE_BASE_PATH + TOPIC_SUPPORT_URL;

    public static final String MISSING_WEB_SOCKET_AUTHENTICATION_TOKEN = "Missing WebSocket authentication token";
    public static final String YOU_ARE_NOT_AUTHORIZED_TO_ACCESS_THIS_CONVERSATION = "You are not authorized to access this conversation";
    public static final String INVALID_CONVERSATION_DESTINATION = "Invalid conversation destination";
    public static final String UNAUTHENTICATED_WEB_SOCKET_CONNECTION = "Unauthenticated WebSocket connection";
    public static final String UNAUTHENTICATED_USER = "Unauthenticated user";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith(BEARER + SPACE)) {
                throw new MessageDeliveryException(MISSING_WEB_SOCKET_AUTHENTICATION_TOKEN);
            }

            String token = authHeader.substring(7);
            String username = jwtService.extractUserName(token);
            if (username == null || commonService.isTokenBlacklisted(token, username)) {
                throw new MessageDeliveryException(TOKEN_BLACKLISTED_MESSAGE);
            }

            MyUserDetailsService userDetailsService = applicationContext.getBean(MyUserDetailsService.class);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!jwtService.validateToken(token, userDetails)) {
                throw new MessageDeliveryException(StringConstants.INVALID_OR_EXPIRED_TOKEN);
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            accessor.setUser(authentication);
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            String destination = accessor.getDestination();
            if (destination != null && destination.startsWith(SUPPORT_TOPIC_PREFIX)) {
                String conversationId = destination.substring(SUPPORT_TOPIC_PREFIX.length());
                String username = getUsername(conversationId, accessor);
                Long userId = userAuthService.getUserIdByUsername(username);
                SupportConversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new MessageDeliveryException(CONVERSATION_NOT_FOUND));

                log.info(
                        "Support subscription - conversationId: {}, userId: {}, conversationUserId: {}, conversationAgentId: {}",
                        conversationId,
                        userId,
                        conversation.getUserId(),
                        conversation.getAgentId()
                );

                boolean authorized = userId.equals(conversation.getUserId()) || userId.equals(conversation.getAgentId());
                if (!authorized) {
                    throw new MessageDeliveryException(YOU_ARE_NOT_AUTHORIZED_TO_ACCESS_THIS_CONVERSATION);
                }
            }
        }
        return message;
    }

    private static String getUsername(String conversationId, StompHeaderAccessor accessor) {
        if (conversationId.isBlank() || conversationId.contains(SLASH)) {
            throw new MessageDeliveryException(INVALID_CONVERSATION_DESTINATION);
        }
        if (accessor.getUser() == null) {
            throw new MessageDeliveryException(UNAUTHENTICATED_WEB_SOCKET_CONNECTION);
        }

        Principal principal = accessor.getUser();
        if (principal == null) {
            try {
                throw new AccessDeniedException(UNAUTHENTICATED_USER);
            } catch (AccessDeniedException e) {
                throw new RuntimeException(e);
            }
        }
        return principal.getName();
    }
}
