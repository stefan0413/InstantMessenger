package org.instantmessenger.backend.Controller;

import org.instantmessenger.backend.Model.Message;
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
            @RequestParam(required = false) Long before
    ) {
        return messageService.getByChannelId(channelId, limit, before);
    }
}
