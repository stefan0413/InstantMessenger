package org.instantmessenger.backend.DTO;

public record TypingEvent(Long userId, long channelId, boolean typing) {}
