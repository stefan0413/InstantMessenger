package org.instantmessenger.backend.service;

import org.instantmessenger.backend.DTO.ChannelRequest;
import org.instantmessenger.backend.DTO.ChannelResponse;
import org.instantmessenger.backend.Repository.ChannelRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChannelService {

    private final ChannelRepository channelRepository;

    public ChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    public ChannelResponse create(ChannelRequest request) {
        var channel = channelRepository.create(request.name());
        channelRepository.addMembers(channel.id(), request.memberIds());
        return new ChannelResponse(channel.id(), channel.name(), request.memberIds());
    }

    public List<ChannelResponse> getAll() {
        Map<Long, List<Long>> memberIds = channelRepository.findAllMemberIds();
        return channelRepository.findAll().stream()
                .map(c -> new ChannelResponse(c.id(), c.name(), memberIds.getOrDefault(c.id(), List.of())))
                .toList();
    }
}
