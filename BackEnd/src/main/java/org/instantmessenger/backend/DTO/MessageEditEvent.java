package org.instantmessenger.backend.DTO;

public record MessageEditEvent(long messageId, long channelId, String content) {}
