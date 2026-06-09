package org.instantmessenger.backend.config;

import org.instantmessenger.backend.Repository.ChannelRepository;
import org.instantmessenger.backend.Repository.UserRepository;
import org.instantmessenger.backend.service.JwtService;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    public WebSocketConfig(JwtService jwtService, UserRepository userRepository, ChannelRepository channelRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.channelRepository = channelRepository;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) {
                    return message;
                }
                var command = accessor.getCommand();

                if (StompCommand.CONNECT.equals(command)) {
                    authenticate(accessor);
                } else if (StompCommand.SEND.equals(command) || StompCommand.SUBSCRIBE.equals(command)) {
                    authorize(accessor);
                }

                return message;
            }
        });
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing WebSocket authorization token");
        }

        long userId = jwtService.validateAndGetUserId(header.substring("Bearer ".length()));
        if (userRepository.findById(userId).isEmpty()) {
            throw new IllegalArgumentException("Invalid WebSocket authorization token");
        }

        var attributes = accessor.getSessionAttributes();
        if (attributes != null) {
            attributes.put(AuthenticatedUser.ATTRIBUTE, userId);
        }
        accessor.setUser(authenticatedPrincipal(userId));
    }

    private void authorize(StompHeaderAccessor accessor) {
        long userId = authenticatedUserId(accessor);
        String destination = accessor.getDestination();

        if (destination == null) {
            return;
        }

        if (destination.startsWith("/topic/channel/")) {
            long channelId = Long.parseLong(destination.substring("/topic/channel/".length()));
            if (!channelRepository.isMember(channelId, userId)) {
                throw new IllegalArgumentException("Cannot subscribe to a channel the user does not belong to");
            }
        }

        if (destination.startsWith("/topic/user/")) {
            long destinationUserId = Long.parseLong(destination.substring("/topic/user/".length()));
            if (destinationUserId != userId) {
                throw new IllegalArgumentException("Cannot subscribe to another user's notifications");
            }
        }
    }

    private long authenticatedUserId(StompHeaderAccessor accessor) {
        var attributes = accessor.getSessionAttributes();
        Object userId = attributes != null ? attributes.get(AuthenticatedUser.ATTRIBUTE) : null;
        if (userId instanceof Long value) {
            accessor.setUser(authenticatedPrincipal(value));
            return value;
        }
        throw new IllegalArgumentException("WebSocket session is not authenticated");
    }

    private Principal authenticatedPrincipal(long userId) {
        return () -> Long.toString(userId);
    }
}
