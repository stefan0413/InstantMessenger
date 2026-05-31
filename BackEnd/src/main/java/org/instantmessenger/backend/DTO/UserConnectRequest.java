package org.instantmessenger.backend.DTO;

import jakarta.validation.constraints.NotNull;

public record UserConnectRequest(@NotNull long userId) {}
