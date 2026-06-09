package org.instantmessenger.backend.service;

import org.instantmessenger.backend.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresenceServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks PresenceService presenceService;

    @Test
    void register_storesSessionToUserMapping() {
        when(userRepository.existsById(1L)).thenReturn(true);

        presenceService.register("session-1", 1L);

        assertThat(presenceService.getOnlineUserIds()).contains(1L);
    }

    @Test
    void register_throwsWhenUserDoesNotExist() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> presenceService.register("session-x", 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void unregister_returnsUserIdAndRemovesFromOnlineSet() {
        when(userRepository.existsById(2L)).thenReturn(true);
        presenceService.register("session-2", 2L);

        Optional<Long> result = presenceService.unregister("session-2");

        assertThat(result).contains(2L);
        assertThat(presenceService.getOnlineUserIds()).doesNotContain(2L);
    }

    @Test
    void unregister_returnsEmptyForUnknownSession() {
        Optional<Long> result = presenceService.unregister("no-such-session");

        assertThat(result).isEmpty();
    }

    @Test
    void getOnlineUserIds_returnsAllRegisteredUsers() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(2L)).thenReturn(true);
        when(userRepository.existsById(3L)).thenReturn(true);

        presenceService.register("s1", 1L);
        presenceService.register("s2", 2L);
        presenceService.register("s3", 3L);

        assertThat(presenceService.getOnlineUserIds()).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void unregister_doesNotAffectOtherActiveSessions() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(2L)).thenReturn(true);

        presenceService.register("s1", 1L);
        presenceService.register("s2", 2L);

        presenceService.unregister("s1");

        assertThat(presenceService.getOnlineUserIds())
                .doesNotContain(1L)
                .contains(2L);
    }

    @Test
    void unregister_calledTwiceForSameSession_returnsEmptyOnSecondCall() {
        when(userRepository.existsById(5L)).thenReturn(true);
        presenceService.register("session-5", 5L);
        presenceService.unregister("session-5");

        Optional<Long> secondUnregister = presenceService.unregister("session-5");

        assertThat(secondUnregister).isEmpty();
    }

    @Test
    void getOnlineUserIds_returnsEmptySetWhenNoUsersRegistered() {
        assertThat(presenceService.getOnlineUserIds()).isEmpty();
    }

    @Test
    void getOnlineUserIds_afterAllUsersDisconnect_returnsEmpty() {
        when(userRepository.existsById(1L)).thenReturn(true);
        presenceService.register("s1", 1L);
        presenceService.unregister("s1");

        assertThat(presenceService.getOnlineUserIds()).isEmpty();
    }
}
