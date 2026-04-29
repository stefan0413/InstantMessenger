package org.instantmessenger.backend.service;

import org.instantmessenger.backend.Model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessagingService {

    private static final Logger log = LoggerFactory.getLogger(MessagingService.class);
    private static final String DESTINATION_TEMPLATE = "/topic/channel/%d";

    private final SimpMessagingTemplate messagingTemplate;

    public MessagingService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcast(Message message) {
        var destination = String.format(DESTINATION_TEMPLATE, message.channelId());
        log.debug("Broadcasting message {} to {}", message.id(), destination);
        messagingTemplate.convertAndSend(destination, message);
        log.info("Message {} broadcast to {}", message.id(), destination);
    }
}
