package org.instantmessenger.backend.service;

import org.instantmessenger.backend.Model.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessagingService {

    private static final String DESTINATION_TEMPLATE = "/topic/channel/%d";
    private final SimpMessagingTemplate messagingTemplate;

    public MessagingService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcast(Message message) {
        var destination = String.format(DESTINATION_TEMPLATE, message.channelId());
        messagingTemplate.convertAndSend(destination, message);
    }
}
