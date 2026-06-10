package org.instantmessenger.backend.dto;

import jakarta.validation.constraints.NotNull;

public record MessageRequest(
        String content,
        @NotNull
        long channelId,
        String fileUrl,
        String fileName
) {}
