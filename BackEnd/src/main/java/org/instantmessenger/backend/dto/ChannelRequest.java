package org.instantmessenger.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ChannelRequest(
        @NotBlank
        String name,
        @NotEmpty
        List<Long> memberIds
) {}
