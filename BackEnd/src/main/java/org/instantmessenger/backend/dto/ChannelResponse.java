package org.instantmessenger.backend.dto;

import java.util.List;

public record ChannelResponse(
        long id,
        String name,
        List<Long> memberIds,
        List<UserView> members
) {}
