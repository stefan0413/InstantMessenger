package org.instantmessenger.backend.controller;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.instantmessenger.backend.dto.ChannelRequest;
import org.instantmessenger.backend.dto.ChannelResponse;
import org.instantmessenger.backend.config.AuthenticatedUser;
import org.instantmessenger.backend.service.ChannelService;
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
    public List<ChannelResponse> getAll(HttpServletRequest request) {
        return channelService.getForUser(AuthenticatedUser.from(request));
    }

    @PostMapping
    public ChannelResponse create(@Valid @RequestBody ChannelRequest channelRequest, HttpServletRequest request) {
        return channelService.create(channelRequest, AuthenticatedUser.from(request));
    }
}
