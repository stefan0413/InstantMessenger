package org.instantmessenger.backend.Controller;

import org.instantmessenger.backend.DTO.*;
import org.instantmessenger.backend.service.MessageService;
import org.instantmessenger.backend.service.MessagingService;
import org.instantmessenger.backend.service.PresenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final MessageService messageService;
    private final MessagingService messagingService;
    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(MessageService messageService,
                          MessagingService messagingService,
                          PresenceService presenceService,
                          SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingService = messagingService;
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageRequest request) {
        log.info("Received message from user {} for channel {}", request.userId(), request.channelId());
        messageService.processAndBroadcast(request);
    }

    @MessageMapping("/chat.edit")
    public void editMessage(@Payload MessageEditRequest request) {
        log.info("Edit request for message {} by user {}", request.messageId(), request.userId());
        messageService.editMessage(request);
    }

    @MessageMapping("/chat.delete")
    public void deleteMessage(@Payload MessageDeleteRequest request) {
        log.info("Delete request for message {} by user {}", request.messageId(), request.userId());
        messageService.deleteMessage(request);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload TypingEvent event) {
        messagingService.broadcastTyping(event);
    }

    @MessageMapping("/user.connect")
    public void onUserConnect(@Payload UserConnectRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.info("User {} registering presence for session {}", request.userId(), sessionId);
        presenceService.register(sessionId, request.userId());
        messagingService.broadcastPresence(new PresenceEvent(request.userId(), "ONLINE"));
    }

    // Returns recent message history when a client subscribes to a channel.
    // FE subscribes to /app/channel/{channelId} to trigger this, then separately to /topic/channel/{channelId} for live updates.
    @SubscribeMapping("/channel/{channelId}")
    public List<ChannelEvent> onChannelSubscribe(@DestinationVariable long channelId) {
        log.debug("Client subscribed to channel {}, sending history", channelId);
        return messageService.getByChannelId(channelId, 50, null).stream()
                .map(m -> new ChannelEvent("MESSAGE_NEW", m))
                .toList();
    }

    @MessageExceptionHandler
    public void handleException(Exception ex, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.warn("WebSocket error in session {}: {}", sessionId, ex.getMessage());

        var responseAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        responseAccessor.setSessionId(sessionId);
        responseAccessor.setLeaveMutable(true);

        messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/errors",
                new WebSocketErrorResponse(ex.getMessage()),
                responseAccessor.getMessageHeaders()
        );
    }
}
