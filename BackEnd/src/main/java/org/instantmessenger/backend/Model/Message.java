package org.instantmessenger.backend.Model;

import java.time.LocalDateTime;

public record Message (Long id, String content, long userId, long channelId, LocalDateTime time) {
}