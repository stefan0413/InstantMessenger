package org.instantmessenger.backend.Controller;

import org.instantmessenger.backend.Model.Channel;
import org.instantmessenger.backend.Repository.ChannelRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/channels")
public class ChannelController {

    private final ChannelRepository repo;

    public ChannelController(ChannelRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Channel> getAll() {
        return repo.findAll();
    }
}