package org.instantmessenger.backend.Controller;

import org.instantmessenger.backend.DTO.MessageRequest;
import org.instantmessenger.backend.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final MessageService messageService;

    public ChatController(MessageService messageService) {
        this.messageService = messageService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageRequest request) {
        log.info("Received message from user {} for channel {}", request.userId(), request.channelId());
        messageService.processAndBroadcast(request);
    }
}
