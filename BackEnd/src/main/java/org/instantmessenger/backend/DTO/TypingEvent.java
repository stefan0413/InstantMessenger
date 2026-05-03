package org.instantmessenger.backend.DTO;

public record TypingEvent(long userId, long channelId, boolean typing) {}
