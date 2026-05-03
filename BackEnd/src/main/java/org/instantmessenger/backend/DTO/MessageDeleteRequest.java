package org.instantmessenger.backend.DTO;

import jakarta.validation.constraints.NotNull;

public record MessageDeleteRequest(
        @NotNull long messageId,
        @NotNull long userId
) {}
