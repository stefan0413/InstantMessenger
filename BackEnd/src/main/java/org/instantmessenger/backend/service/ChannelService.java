package org.instantmessenger.backend.service;

import org.instantmessenger.backend.DTO.ChannelRequest;
import org.instantmessenger.backend.DTO.ChannelResponse;
import org.instantmessenger.backend.DTO.UserView;
import org.instantmessenger.backend.Repository.ChannelRepository;
import org.instantmessenger.backend.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    public ChannelService(ChannelRepository channelRepository, UserRepository userRepository) {
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
    }

    public ChannelResponse create(ChannelRequest request, long currentUserId) {
        if (!request.memberIds().contains(currentUserId)) {
            throw new IllegalArgumentException("Authenticated user must be a channel member");
        }

        var channel = channelRepository.create(request.name());
        channelRepository.addMembers(channel.id(), request.memberIds());
        var members = userRepository.findByChannelIds(List.of(channel.id()))
                .getOrDefault(channel.id(), List.of())
                .stream()
                .map(user -> new UserView(user.id(), user.username(), user.email()))
                .toList();
        return new ChannelResponse(channel.id(), channel.name(), request.memberIds(), members);
    }

    public List<ChannelResponse> getForUser(long userId) {
        var channels = channelRepository.findByMemberId(userId);
        Map<Long, List<Long>> memberIds = channelRepository.findAllMemberIds();
        Map<Long, List<UserView>> membersByChannel = userRepository.findByChannelIds(
                        channels.stream().map(c -> c.id()).toList()
                ).entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(user -> new UserView(user.id(), user.username(), user.email()))
                                .toList()
                ));

        return channels.stream()
                .map(c -> new ChannelResponse(
                        c.id(),
                        c.name(),
                        memberIds.getOrDefault(c.id(), List.of()),
                        membersByChannel.getOrDefault(c.id(), List.of())
                ))
                .toList();
    }
}
