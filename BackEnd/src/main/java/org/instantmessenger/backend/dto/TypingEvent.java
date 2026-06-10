package org.instantmessenger.backend.dto;

public record TypingEvent(Long userId, long channelId, boolean typing) {}
