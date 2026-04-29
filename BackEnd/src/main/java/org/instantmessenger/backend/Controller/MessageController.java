package org.instantmessenger.backend.Controller;

import org.instantmessenger.backend.DTO.MessageRequest;
import org.instantmessenger.backend.Model.Message;
import org.instantmessenger.backend.Repository.MessageRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageRepository repo;

    public MessageController(MessageRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Message> getMessages(@RequestParam Long channelId) {
        return repo.findByChannelId(channelId);
    }

    @PostMapping
    public void sendMessage(@RequestBody MessageRequest request) {

        Message message = new Message();
        message.setContent(request.content());
        message.setUserId(request.userId());
        message.setChannelId(request.channelId());
        message.setTimestamp(LocalDateTime.now());

        repo.save(message);
    }
}