package org.instantmessenger.backend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MessageEditRequest(
        @NotNull long messageId,
        @NotBlank String content,
        @NotNull long userId
) {}
