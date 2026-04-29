package org.instantmessenger.backend.controller;

import org.instantmessenger.backend.repository.ChannelRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {

    private final ChannelRepository repo;

    public TestController(ChannelRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/test")
    public List<String> test() {
        return repo.findAllNames();
    }
}