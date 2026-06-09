package org.instantmessenger.backend.service;

import org.instantmessenger.backend.DTO.ChannelEvent;
import org.instantmessenger.backend.DTO.ChannelResponse;
import org.instantmessenger.backend.DTO.PresenceEvent;
import org.instantmessenger.backend.DTO.TypingEvent;
import org.instantmessenger.backend.Model.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessagingServiceTest {

    @Mock SimpMessagingTemplate messagingTemplate;
    @InjectMocks MessagingService messagingService;

    @Test
    void broadcast_sendsMessageNewEventToChannelTopic() {
        var message = new Message(1L, "hello", 2L, 10L, LocalDateTime.now(), null, null);

        messagingService.broadcast(message);

        var eventCaptor = ArgumentCaptor.forClass(ChannelEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/channel/10"), eventCaptor.capture());
        assertThat(eventCaptor.getValue().type()).isEqualTo("MESSAGE_NEW");
        assertThat(eventCaptor.getValue().data()).isEqualTo(message);
    }

    @Test
    void broadcastTyping_sendsTypingEventToChannelTopic() {
        var event = new TypingEvent(1L, 10L, true);

        messagingService.broadcastTyping(event);

        var eventCaptor = ArgumentCaptor.forClass(ChannelEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/channel/10"), eventCaptor.capture());
        assertThat(eventCaptor.getValue().type()).isEqualTo("TYPING");
        assertThat(eventCaptor.getValue().data()).isEqualTo(event);
    }

    @Test
    void broadcastTyping_typingFalse_sendsToCorrectChannel() {
        var event = new TypingEvent(1L, 42L, false);

        messagingService.broadcastTyping(event);

        var eventCaptor = ArgumentCaptor.forClass(ChannelEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/channel/42"), eventCaptor.capture());
        assertThat(eventCaptor.getValue().type()).isEqualTo("TYPING");
        assertThat(((TypingEvent) eventCaptor.getValue().data()).typing()).isFalse();
    }

    @Test
    void broadcastChannelCreated_notifiesEachMemberOnTheirUserTopic() {
        var channel = new ChannelResponse(42L, "general", List.of(1L, 2L, 3L), List.of());

        messagingService.broadcastChannelCreated(channel);

        var destCaptor = ArgumentCaptor.forClass(String.class);
        var eventCaptor = ArgumentCaptor.forClass(ChannelEvent.class);
        verify(messagingTemplate, times(3)).convertAndSend(destCaptor.capture(), eventCaptor.capture());
        assertThat(destCaptor.getAllValues())
                .containsExactlyInAnyOrder("/topic/user/1", "/topic/user/2", "/topic/user/3");
        assertThat(eventCaptor.getAllValues())
                .allMatch(e -> "CHANNEL_NEW".equals(e.type()))
                .allMatch(e -> e.data().equals(channel));
    }

    @Test
    void broadcastChannelCreated_withNoMembers_sendsNothing() {
        var channel = new ChannelResponse(1L, "empty", List.of(), List.of());

        messagingService.broadcastChannelCreated(channel);

        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void broadcastChannelCreated_withOneMember_notifiesOnlyThatMember() {
        var channel = new ChannelResponse(7L, "dm", List.of(99L), List.of());

        messagingService.broadcastChannelCreated(channel);

        var eventCaptor = ArgumentCaptor.forClass(ChannelEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/user/99"), eventCaptor.capture());
        assertThat(eventCaptor.getValue().type()).isEqualTo("CHANNEL_NEW");
    }

    @Test
    void broadcastPresence_online_sendsToPresenceTopic() {
        var event = new PresenceEvent(7L, "ONLINE");

        messagingService.broadcastPresence(event);

        verify(messagingTemplate).convertAndSend("/topic/presence", event);
    }

    @Test
    void broadcastPresence_offline_sendsToPresenceTopic() {
        var event = new PresenceEvent(7L, "OFFLINE");

        messagingService.broadcastPresence(event);

        verify(messagingTemplate).convertAndSend("/topic/presence", event);
    }

    @Test
    void broadcast_usesChannelIdFromMessage() {
        var messageInChannel99 = new Message(1L, "hi", 1L, 99L, LocalDateTime.now(), null, null);

        messagingService.broadcast(messageInChannel99);

        verify(messagingTemplate).convertAndSend(eq("/topic/channel/99"), any(ChannelEvent.class));
    }
}
