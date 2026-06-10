package org.instantmessenger.backend.service;

import org.instantmessenger.backend.dto.ChannelRequest;
import org.instantmessenger.backend.dto.ChannelResponse;
import org.instantmessenger.backend.dto.UserView;
import org.instantmessenger.backend.repository.ChannelRepository;
import org.instantmessenger.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessagingService messagingService;

    public ChannelService(ChannelRepository channelRepository, UserRepository userRepository, MessagingService messagingService) {
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
        this.messagingService = messagingService;
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
        var response = new ChannelResponse(channel.id(), channel.name(), request.memberIds(), members);
        messagingService.broadcastChannelCreated(response);
        return response;
    }

    public List<ChannelResponse> getForUser(long userId) {
        var channels = channelRepository.findByMemberId(userId);
        if (channels.isEmpty()) {
            return List.of();
        }

        var channelIds = channels.stream().map(c -> c.id()).toList();

        Map<Long, List<Long>> memberIds = channelRepository.findMemberIdsForChannels(channelIds);
        Map<Long, List<UserView>> membersByChannel = userRepository.findByChannelIds(channelIds)
                .entrySet().stream()
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
