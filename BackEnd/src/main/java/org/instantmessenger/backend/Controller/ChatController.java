package org.instantmessenger.backend.Controller;

import org.instantmessenger.backend.DTO.*;
import org.instantmessenger.backend.config.AuthenticatedUser;
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
    public void sendMessage(@Payload MessageRequest request, SimpMessageHeaderAccessor headerAccessor) {
        long currentUserId = AuthenticatedUser.from(headerAccessor);
        log.info("Received message from user {} for channel {}", currentUserId, request.channelId());
        messageService.processAndBroadcast(request, currentUserId);
    }

    @MessageMapping("/chat.edit")
    public void editMessage(@Payload MessageEditRequest request, SimpMessageHeaderAccessor headerAccessor) {
        long currentUserId = AuthenticatedUser.from(headerAccessor);
        log.info("Edit request for message {} by user {}", request.messageId(), currentUserId);
        messageService.editMessage(request, currentUserId);
    }

    @MessageMapping("/chat.delete")
    public void deleteMessage(@Payload MessageDeleteRequest request, SimpMessageHeaderAccessor headerAccessor) {
        long currentUserId = AuthenticatedUser.from(headerAccessor);
        log.info("Delete request for message {} by user {}", request.messageId(), currentUserId);
        messageService.deleteMessage(request, currentUserId);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload TypingEvent event, SimpMessageHeaderAccessor headerAccessor) {
        long currentUserId = AuthenticatedUser.from(headerAccessor);
        messageService.ensureChannelMember(event.channelId(), currentUserId);
        messagingService.broadcastTyping(new TypingEvent(currentUserId, event.channelId(), event.typing()));
    }

    @MessageMapping("/user.connect")
    public void onUserConnect(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        long currentUserId = AuthenticatedUser.from(headerAccessor);
        log.info("User {} registering presence for session {}", currentUserId, sessionId);
        presenceService.register(sessionId, currentUserId);
        presenceService.getOnlineUserIds()
                .forEach(uid -> messagingService.broadcastPresence(new PresenceEvent(uid, "ONLINE")));
    }

    @SubscribeMapping("/channel/{channelId}")
    public List<ChannelEvent> onChannelSubscribe(@DestinationVariable long channelId, SimpMessageHeaderAccessor headerAccessor) {
        log.debug("Client subscribed to channel {}, sending history", channelId);
        return messageService.getByChannelId(channelId, AuthenticatedUser.from(headerAccessor), 50, null).stream()
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
