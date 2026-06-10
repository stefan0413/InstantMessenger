package org.instantmessenger.backend.config;

import org.instantmessenger.backend.dto.PresenceEvent;
import org.instantmessenger.backend.service.MessagingService;
import org.instantmessenger.backend.service.PresenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final PresenceService presenceService;
    private final MessagingService messagingService;

    public WebSocketEventListener(PresenceService presenceService, MessagingService messagingService) {
        this.presenceService = presenceService;
        this.messagingService = messagingService;
    }

    @EventListener
    public void onConnect(SessionConnectedEvent event) {
        var accessor = StompHeaderAccessor.wrap(event.getMessage());
        log.debug("WebSocket session connected: {}", accessor.getSessionId());
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        var accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        log.debug("WebSocket session disconnected: {}", sessionId);

        presenceService.unregister(sessionId).ifPresent(userId -> {
            log.info("User {} went offline (session {})", userId, sessionId);
            messagingService.broadcastPresence(new PresenceEvent(userId, "OFFLINE"));
        });
    }
}
