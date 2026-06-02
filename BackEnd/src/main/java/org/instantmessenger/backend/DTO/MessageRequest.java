package org.instantmessenger.backend.DTO;

import jakarta.validation.constraints.NotNull;

public record MessageRequest(
        String content,
        @NotNull
        long userId,
        @NotNull
        long channelId,
        String fileUrl,
        String fileName
) {}