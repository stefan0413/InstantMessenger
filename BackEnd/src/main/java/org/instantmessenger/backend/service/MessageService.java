package org.instantmessenger.backend.service;

import org.instantmessenger.backend.DTO.MessageRequest;
import org.instantmessenger.backend.Repository.ChannelRepository;
import org.instantmessenger.backend.Repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final MessagingService messagingService;

    public MessageService(MessageRepository messageRepository,
                          ChannelRepository channelRepository,
                          MessagingService messagingService) {
        this.messageRepository = messageRepository;
        this.channelRepository = channelRepository;
        this.messagingService = messagingService;
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

    private void validateMessageRequest(MessageRequest request) {
        if (!channelRepository.existsById(request.channelId())) {
            log.warn("Rejected message — channel {} does not exist", request.channelId());
            throw new IllegalArgumentException("Channel not found: " + request.channelId());
        }
        //TODO: Validate user exists and it is in the channel
    }
}
