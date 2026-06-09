package org.instantmessenger.backend.config;

import org.instantmessenger.backend.DTO.PresenceEvent;
import org.instantmessenger.backend.service.MessagingService;
import org.instantmessenger.backend.service.PresenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketEventListenerTest {

    @Mock PresenceService presenceService;
    @Mock MessagingService messagingService;
    @InjectMocks WebSocketEventListener listener;

    private static Message<byte[]> buildStompMessage(StompCommand command, String sessionId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setSessionId(sessionId);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    void onConnect_completesWithoutErrorAndDoesNotTouchServices() {
        var message = buildStompMessage(StompCommand.CONNECTED, "sess-1");
        var event = new SessionConnectedEvent(this, message);

        assertThatNoException().isThrownBy(() -> listener.onConnect(event));

        verifyNoInteractions(presenceService, messagingService);
    }

    @Test
    void onDisconnect_withRegisteredUser_unregistersAndBroadcastsOffline() {
        var message = buildStompMessage(StompCommand.DISCONNECT, "sess-2");
        var event = new SessionDisconnectEvent(this, message, "sess-2", CloseStatus.NORMAL);
        when(presenceService.unregister("sess-2")).thenReturn(Optional.of(10L));

        listener.onDisconnect(event);

        verify(presenceService).unregister("sess-2");
        var captor = ArgumentCaptor.forClass(PresenceEvent.class);
        verify(messagingService).broadcastPresence(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(10L);
        assertThat(captor.getValue().status()).isEqualTo("OFFLINE");
    }

    @Test
    void onDisconnect_withNoRegisteredUser_doesNotBroadcast() {
        var message = buildStompMessage(StompCommand.DISCONNECT, "sess-3");
        var event = new SessionDisconnectEvent(this, message, "sess-3", CloseStatus.NORMAL);
        when(presenceService.unregister("sess-3")).thenReturn(Optional.empty());

        listener.onDisconnect(event);

        verify(presenceService).unregister("sess-3");
        verifyNoInteractions(messagingService);
    }

    @Test
    void onDisconnect_withAbnormalClose_stillUnregistersAndBroadcasts() {
        var message = buildStompMessage(StompCommand.DISCONNECT, "sess-4");
        var event = new SessionDisconnectEvent(this, message, "sess-4", CloseStatus.SERVER_ERROR);
        when(presenceService.unregister("sess-4")).thenReturn(Optional.of(20L));

        listener.onDisconnect(event);

        var captor = ArgumentCaptor.forClass(PresenceEvent.class);
        verify(messagingService).broadcastPresence(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(20L);
        assertThat(captor.getValue().status()).isEqualTo("OFFLINE");
    }

    @Test
    void onDisconnect_broadcastsExactlyOncePerDisconnect() {
        var message = buildStompMessage(StompCommand.DISCONNECT, "sess-5");
        var event = new SessionDisconnectEvent(this, message, "sess-5", CloseStatus.NORMAL);
        when(presenceService.unregister("sess-5")).thenReturn(Optional.of(30L));

        listener.onDisconnect(event);

        verify(messagingService, times(1)).broadcastPresence(any());
    }
}
