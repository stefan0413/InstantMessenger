package org.instantmessenger.backend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MessageRequest(
        @NotBlank
        String content,
        @NotNull
        long userId,
        @NotNull
        long channelId
) {}