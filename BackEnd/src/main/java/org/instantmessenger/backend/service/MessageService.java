package org.instantmessenger.backend.service;

import org.instantmessenger.backend.DTO.MessageRequest;
import org.instantmessenger.backend.Model.Message;
import org.instantmessenger.backend.Repository.ChannelRepository;
import org.instantmessenger.backend.Repository.MessageRepository;
import org.instantmessenger.backend.Repository.UserRepository;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessagingService messagingService;

    public MessageService(MessageRepository messageRepository,
                          ChannelRepository channelRepository,
                          UserRepository userRepository,
                          MessagingService messagingService) {
        this.messageRepository = messageRepository;
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
        this.messagingService = messagingService;
    }

    public List<Message> getByChannelId(Long channelId, long currentUserId, int limit, Long before) {
        ensureChannelMember(channelId, currentUserId);
        return messageRepository.findByChannelId(channelId, Math.min(limit, 100), before);
    }

    public List<Message> searchByChannel(Long channelId, String query, long currentUserId, int limit) {
        ensureChannelMember(channelId, currentUserId);

        if (query == null || query.isBlank()) {
            return List.of();
        }

        return messageRepository.searchByChannel(channelId, query.trim(), Math.min(limit, 100));
    }

    public void ensureChannelMember(long channelId, long currentUserId) {
        if (!channelRepository.isMember(channelId, currentUserId)) {
            throw new IllegalArgumentException("User is not a member of this channel");
        }
    }

    public void processAndBroadcast(MessageRequest request, long currentUserId) {
        log.debug("Processing message for channel {} from user {}", request.channelId(), currentUserId);

        validateMessageRequest(request, currentUserId);

        var id = messageRepository.save(request, currentUserId);
        log.debug("Message saved with id {}", id);

        var message = messageRepository.getByIdOrElseThrow(id);
        messagingService.broadcast(message);

        log.info("Message {} processed and broadcast to channel {}", id, request.channelId());
    }

    private void validateMessageRequest(MessageRequest request, long currentUserId) {
        boolean hasContent = request.content() != null && !request.content().isBlank();
        boolean hasFile = request.fileUrl() != null && !request.fileUrl().isBlank();
        if (!hasContent && !hasFile) {
            throw new IllegalArgumentException("Message must have content or a file attachment");
        }
        if (!channelRepository.existsById(request.channelId())) {
            log.warn("Rejected message — channel {} does not exist", request.channelId());
            throw new IllegalArgumentException("Channel not found: " + request.channelId());
        }
        if (!userRepository.existsById(currentUserId)) {
            log.warn("Rejected message — user {} does not exist", currentUserId);
            throw new IllegalArgumentException("User not found: " + currentUserId);
        }
        ensureChannelMember(request.channelId(), currentUserId);
    }
}
