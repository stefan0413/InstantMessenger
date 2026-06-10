package org.instantmessenger.backend.service;

import org.instantmessenger.backend.dto.ChannelEvent;
import org.instantmessenger.backend.dto.ChannelResponse;
import org.instantmessenger.backend.dto.PresenceEvent;
import org.instantmessenger.backend.dto.TypingEvent;
import org.instantmessenger.backend.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessagingService {

    private static final Logger log = LoggerFactory.getLogger(MessagingService.class);
    private static final String CHANNEL_TOPIC = "/topic/channel/%d";
    private static final String USER_TOPIC = "/topic/user/%d";
    private static final String PRESENCE_TOPIC = "/topic/presence";

    private final SimpMessagingTemplate messagingTemplate;

    public MessagingService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcast(Message message) {
        var destination = String.format(CHANNEL_TOPIC, message.channelId());
        log.debug("Broadcasting MESSAGE_NEW {} to {}", message.id(), destination);
        messagingTemplate.convertAndSend(destination, new ChannelEvent("MESSAGE_NEW", message));
    }

    public void broadcastTyping(TypingEvent event) {
        var destination = String.format(CHANNEL_TOPIC, event.channelId());
        messagingTemplate.convertAndSend(destination, new ChannelEvent("TYPING", event));
    }

    public void broadcastChannelCreated(ChannelResponse channel) {
        channel.memberIds().forEach(memberId -> {
            var destination = String.format(USER_TOPIC, memberId);
            log.debug("Notifying user {} of new channel {}", memberId, channel.id());
            messagingTemplate.convertAndSend(destination, new ChannelEvent("CHANNEL_NEW", channel));
        });
    }

    public void broadcastPresence(PresenceEvent event) {
        log.debug("Broadcasting presence: user {} is {}", event.userId(), event.status());
        messagingTemplate.convertAndSend(PRESENCE_TOPIC, event);
    }
}
