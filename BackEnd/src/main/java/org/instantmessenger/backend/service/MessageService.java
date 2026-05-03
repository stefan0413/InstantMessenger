package org.instantmessenger.backend.service;

import org.instantmessenger.backend.DTO.MessageDeleteRequest;
import org.instantmessenger.backend.DTO.MessageEditEvent;
import org.instantmessenger.backend.DTO.MessageEditRequest;
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

    public List<Message> getByChannelId(Long channelId, int limit, Long before) {
        return messageRepository.findByChannelId(channelId, Math.min(limit, 100), before);
    }

    public void processAndBroadcast(MessageRequest request) {
        log.debug("Processing message for channel {} from user {}", request.channelId(), request.userId());

        validateMessageRequest(request);

        var id = messageRepository.save(request);
        log.debug("Message saved with id {}", id);

        var message = messageRepository.getByIdOrElseThrow(id);
        messagingService.broadcast(message);

        log.info("Message {} processed and broadcast to channel {}", id, request.channelId());
    }

    public void editMessage(MessageEditRequest request) {
        var message = messageRepository.getByIdOrElseThrow(request.messageId());
        if (message.userId() != request.userId()) {
            throw new IllegalArgumentException("Cannot edit another user's message");
        }
        messageRepository.update(request.messageId(), request.content());
        messagingService.broadcastEdit(message.channelId(),
                new MessageEditEvent(request.messageId(), message.channelId(), request.content()));
        log.info("Message {} edited by user {}", request.messageId(), request.userId());
    }

    public void deleteMessage(MessageDeleteRequest request) {
        var message = messageRepository.getByIdOrElseThrow(request.messageId());
        if (message.userId() != request.userId()) {
            throw new IllegalArgumentException("Cannot delete another user's message");
        }
        messageRepository.delete(request.messageId());
        messagingService.broadcastDelete(message.channelId(), request.messageId());
        log.info("Message {} deleted by user {}", request.messageId(), request.userId());
    }

    private void validateMessageRequest(MessageRequest request) {
        if (!channelRepository.existsById(request.channelId())) {
            log.warn("Rejected message — channel {} does not exist", request.channelId());
            throw new IllegalArgumentException("Channel not found: " + request.channelId());
        }
        if (!userRepository.existsById(request.userId())) {
            log.warn("Rejected message — user {} does not exist", request.userId());
            throw new IllegalArgumentException("User not found: " + request.userId());
        }
        if (!channelRepository.isMember(request.channelId(), request.userId())) {
            log.warn("Rejected message — user {} is not a member of channel {}", request.userId(), request.channelId());
            throw new IllegalArgumentException("User is not a member of this channel");
        }
    }
}
