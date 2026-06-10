package org.instantmessenger.backend.service;

import org.instantmessenger.backend.dto.MessageRequest;
import org.instantmessenger.backend.model.Message;
import org.instantmessenger.backend.repository.ChannelRepository;
import org.instantmessenger.backend.repository.MessageRepository;
import org.instantmessenger.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock MessageRepository messageRepository;
    @Mock ChannelRepository channelRepository;
    @Mock UserRepository userRepository;
    @Mock MessagingService messagingService;
    @InjectMocks MessageService messageService;

    @Test
    void processAndBroadcast_savesMessageAndBroadcasts() {
        var request = new MessageRequest("hello", 10L, null, null);
        var saved = new Message(42L, "hello", 1L, 10L, LocalDateTime.now(), null, null);
        when(channelRepository.existsById(10L)).thenReturn(true);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(channelRepository.isMember(10L, 1L)).thenReturn(true);
        when(messageRepository.save(request, 1L)).thenReturn(42L);
        when(messageRepository.getByIdOrElseThrow(42L)).thenReturn(saved);

        messageService.processAndBroadcast(request, 1L);

        verify(messageRepository).save(request, 1L);
        verify(messagingService).broadcast(saved);
    }

    @Test
    void processAndBroadcast_withFileOnly_broadcasts() {
        var request = new MessageRequest(null, 10L, "http://s3/file.pdf", "file.pdf");
        var saved = new Message(43L, null, 1L, 10L, LocalDateTime.now(), "http://s3/file.pdf", "file.pdf");
        when(channelRepository.existsById(10L)).thenReturn(true);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(channelRepository.isMember(10L, 1L)).thenReturn(true);
        when(messageRepository.save(request, 1L)).thenReturn(43L);
        when(messageRepository.getByIdOrElseThrow(43L)).thenReturn(saved);

        messageService.processAndBroadcast(request, 1L);

        verify(messagingService).broadcast(saved);
    }

    @Test
    void processAndBroadcast_withNoContentAndNoFile_throwsBeforeSaving() {
        var request = new MessageRequest(null, 10L, null, null);

        assertThatThrownBy(() -> messageService.processAndBroadcast(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("content or a file");

        verifyNoInteractions(messageRepository, messagingService);
    }

    @Test
    void processAndBroadcast_withBlankContent_throwsBeforeSaving() {
        var request = new MessageRequest("   ", 10L, null, null);

        assertThatThrownBy(() -> messageService.processAndBroadcast(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("content or a file");

        verifyNoInteractions(messageRepository, messagingService);
    }

    @Test
    void processAndBroadcast_channelNotFound_throwsWithoutBroadcast() {
        var request = new MessageRequest("hello", 99L, null, null);
        when(channelRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> messageService.processAndBroadcast(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Channel not found: 99");

        verifyNoInteractions(messagingService);
    }

    @Test
    void processAndBroadcast_userNotFound_throwsWithoutBroadcast() {
        var request = new MessageRequest("hello", 10L, null, null);
        when(channelRepository.existsById(10L)).thenReturn(true);
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> messageService.processAndBroadcast(request, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found: 99");

        verifyNoInteractions(messagingService);
    }

    @Test
    void processAndBroadcast_userNotChannelMember_throwsWithoutBroadcast() {
        var request = new MessageRequest("hello", 10L, null, null);
        when(channelRepository.existsById(10L)).thenReturn(true);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(channelRepository.isMember(10L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> messageService.processAndBroadcast(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a member");

        verifyNoInteractions(messagingService);
    }

    @Test
    void getByChannelId_capsLimitAt100() {
        when(channelRepository.isMember(10L, 1L)).thenReturn(true);
        when(messageRepository.findByChannelId(10L, 100, null)).thenReturn(List.of());

        messageService.getByChannelId(10L, 1L, 200, null);

        verify(messageRepository).findByChannelId(10L, 100, null);
    }

    @Test
    void getByChannelId_passesLimitUnchangedWhenUnder100() {
        when(channelRepository.isMember(10L, 1L)).thenReturn(true);
        when(messageRepository.findByChannelId(10L, 50, null)).thenReturn(List.of());

        messageService.getByChannelId(10L, 1L, 50, null);

        verify(messageRepository).findByChannelId(10L, 50, null);
    }

    @Test
    void getByChannelId_passesBeforeCursorToRepository() {
        when(channelRepository.isMember(10L, 1L)).thenReturn(true);
        when(messageRepository.findByChannelId(10L, 20, 500L)).thenReturn(List.of());

        messageService.getByChannelId(10L, 1L, 20, 500L);

        verify(messageRepository).findByChannelId(10L, 20, 500L);
    }
}
