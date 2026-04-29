package org.instantmessenger.backend.Controller;

import org.instantmessenger.backend.Model.Message;
import org.instantmessenger.backend.Repository.ChannelRepository;
import org.instantmessenger.backend.Repository.MessageRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {

    private final ChannelRepository repo;
    private final MessageRepository msgRepo;

    public TestController(ChannelRepository repo, MessageRepository msgRepo) {
        this.repo = repo;
        this.msgRepo = msgRepo;
    }

    @GetMapping("/test")
    public List<String> test() {
        return repo.findAllNames();
    }

    @GetMapping("/messages")
    public List<Message> messages() {
        return msgRepo.findByChannelId(1L);
    }
}