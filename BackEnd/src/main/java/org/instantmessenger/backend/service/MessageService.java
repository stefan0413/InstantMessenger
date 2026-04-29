package org.instantmessenger.backend.service;

import org.instantmessenger.backend.DTO.MessageRequest;
import org.instantmessenger.backend.Repository.ChannelRepository;
import org.instantmessenger.backend.Repository.MessageRepository;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

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
        validateMessageRequest(request);

        var id = messageRepository.save(request);
        var message = messageRepository.getByIdOrElseThrow(id);

        messagingService.broadcast(message);
    }

    private void validateMessageRequest(MessageRequest request){
        if (!channelRepository.existsById(request.channelId())) {
            throw new IllegalArgumentException("Channel not found: " + request.channelId());
        }
        //TODO: Validate user exists and it is in the channel
    }
}
