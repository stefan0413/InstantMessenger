package org.instantmessenger.backend.Controller;

import jakarta.validation.Valid;
import org.instantmessenger.backend.DTO.ChannelRequest;
import org.instantmessenger.backend.DTO.ChannelResponse;
import org.instantmessenger.backend.service.ChannelService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/channels")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @GetMapping
    public List<ChannelResponse> getAll() {
        return channelService.getAll();
    }

    @PostMapping
    public ChannelResponse create(@Valid @RequestBody ChannelRequest request) {
        return channelService.create(request);
    }
}
