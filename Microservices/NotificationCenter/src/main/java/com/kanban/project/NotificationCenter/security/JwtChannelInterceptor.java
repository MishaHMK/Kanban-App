package com.kanban.project.NotificationCenter.security;

import com.kanban.project.NotificationCenter.error.ExceptionMessage;
import com.kanban.project.NotificationCenter.error.NotificationCenterException;
import com.kanban.project.NotificationCenter.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {
    private final JwtUtil jwtUtil;
    private static final String BEARER_HEADER = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);

            if (authHeader == null || !authHeader.startsWith(BEARER_HEADER)) {
                throw new NotificationCenterException(ExceptionMessage.MISSING_DELIVERY);
            }

            try {
                Claims claims = jwtUtil.extractAllClaims(authHeader.substring(7));
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                claims.getSubject(), null, Collections.emptyList());
                accessor.setUser(auth);
            } catch (JwtException | IllegalArgumentException e) {
                throw new NotificationCenterException(ExceptionMessage.MISSING_DELIVERY);
            }
        }

        return message;
    }
}