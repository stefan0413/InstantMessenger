package org.instantmessenger.backend.dto;

import jakarta.validation.constraints.NotNull;

public record UserConnectRequest(@NotNull long userId) {}
