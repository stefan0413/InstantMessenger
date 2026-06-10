package org.instantmessenger.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.instantmessenger.backend.model.Message;
import org.instantmessenger.backend.config.AuthenticatedUser;
import org.instantmessenger.backend.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public List<Message> getMessages(
            @RequestParam Long channelId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) Long before,
            HttpServletRequest request
    ) {
        return messageService.getByChannelId(channelId, AuthenticatedUser.from(request), limit, before);
    }
}
