package org.instantmessenger.backend.Controller;

import org.instantmessenger.backend.DTO.MessageRequest;
import org.instantmessenger.backend.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final MessageService messageService;

    public ChatController(MessageService messageService) {
        this.messageService = messageService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageRequest request) {
        messageService.processAndBroadcast(request);
    }
}
