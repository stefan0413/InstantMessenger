package org.instantmessenger.backend.DTO;

public record MessageRequest(
        String content,
        Long userId,
        Long channelId
) {}