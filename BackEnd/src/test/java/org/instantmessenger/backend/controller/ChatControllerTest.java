package org.instantmessenger.backend.controller;

import org.instantmessenger.backend.Controller.ChatController;
import org.instantmessenger.backend.DTO.ChannelEvent;
import org.instantmessenger.backend.DTO.MessageRequest;
import org.instantmessenger.backend.DTO.PresenceEvent;
import org.instantmessenger.backend.DTO.TypingEvent;
import org.instantmessenger.backend.DTO.UserConnectRequest;
import org.instantmessenger.backend.DTO.WebSocketErrorResponse;
import org.instantmessenger.backend.Model.Message;
import org.instantmessenger.backend.service.MessageService;
import org.instantmessenger.backend.service.MessagingService;
import org.instantmessenger.backend.service.PresenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock MessageService messageService;
    @Mock MessagingService messagingService;
    @Mock PresenceService presenceService;
    @Mock SimpMessagingTemplate messagingTemplate;
    @InjectMocks ChatController controller;

    @Test
    void sendMessage_delegatesToMessageService() {
        var request = new MessageRequest("hello", 1L, 10L, null, null);

        controller.sendMessage(request);

        verify(messageService).processAndBroadcast(request);
    }

    @Test
    void typing_broadcastsViaMessagingService() {
        var event = new TypingEvent(1L, 10L, true);

        controller.typing(event);

        verify(messagingService).broadcastTyping(event);
    }

    @Test
    void onUserConnect_registersPresenceAndBroadcastsAllOnlineUsers() {
        var request = new UserConnectRequest(7L);
        var accessor = mock(SimpMessageHeaderAccessor.class);
        when(accessor.getSessionId()).thenReturn("session-abc");
        when(presenceService.getOnlineUserIds()).thenReturn(Set.of(7L, 8L));

        controller.onUserConnect(request, accessor);

        verify(presenceService).register("session-abc", 7L);
        var captor = ArgumentCaptor.forClass(PresenceEvent.class);
        verify(messagingService, times(2)).broadcastPresence(captor.capture());
        assertThat(captor.getAllValues()).extracting(PresenceEvent::status).containsOnly("ONLINE");
        assertThat(captor.getAllValues()).extracting(PresenceEvent::userId).containsExactlyInAnyOrder(7L, 8L);
    }

    @Test
    void onUserConnect_withSingleOnlineUser_broadcastsOnlySelf() {
        var request = new UserConnectRequest(3L);
        var accessor = mock(SimpMessageHeaderAccessor.class);
        when(accessor.getSessionId()).thenReturn("session-xyz");
        when(presenceService.getOnlineUserIds()).thenReturn(Set.of(3L));

        controller.onUserConnect(request, accessor);

        verify(presenceService).register("session-xyz", 3L);
        var captor = ArgumentCaptor.forClass(PresenceEvent.class);
        verify(messagingService).broadcastPresence(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(3L);
        assertThat(captor.getValue().status()).isEqualTo("ONLINE");
    }

    @Test
    void onChannelSubscribe_returnsHistoryWrappedAsMessageNewEvents() {
        var msg1 = new Message(1L, "hi", 1L, 10L, LocalDateTime.now(), null, null);
        var msg2 = new Message(2L, "hey", 2L, 10L, LocalDateTime.now(), null, null);
        when(messageService.getByChannelId(10L, 50, null)).thenReturn(List.of(msg1, msg2));

        List<ChannelEvent> result = controller.onChannelSubscribe(10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).type()).isEqualTo("MESSAGE_NEW");
        assertThat(result.get(0).data()).isEqualTo(msg1);
        assertThat(result.get(1).type()).isEqualTo("MESSAGE_NEW");
        assertThat(result.get(1).data()).isEqualTo(msg2);
    }

    @Test
    void onChannelSubscribe_withNoMessages_returnsEmptyList() {
        when(messageService.getByChannelId(99L, 50, null)).thenReturn(List.of());

        List<ChannelEvent> result = controller.onChannelSubscribe(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void handleException_sendsErrorMessageToUserQueue() {
        var accessor = mock(SimpMessageHeaderAccessor.class);
        when(accessor.getSessionId()).thenReturn("session-err");

        controller.handleException(new RuntimeException("something broke"), accessor);

        var payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSendToUser(
                eq("session-err"),
                eq("/queue/errors"),
                payloadCaptor.capture(),
                any(MessageHeaders.class)
        );
        assertThat(((WebSocketErrorResponse) payloadCaptor.getValue()).error())
                .isEqualTo("something broke");
    }

    @Test
    void handleException_withNullMessage_sendsNullError() {
        var accessor = mock(SimpMessageHeaderAccessor.class);
        when(accessor.getSessionId()).thenReturn("session-null");

        controller.handleException(new NullPointerException(), accessor);

        var payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSendToUser(
                eq("session-null"),
                eq("/queue/errors"),
                payloadCaptor.capture(),
                any(MessageHeaders.class)
        );
        assertThat(((WebSocketErrorResponse) payloadCaptor.getValue()).error()).isNull();
    }
}
