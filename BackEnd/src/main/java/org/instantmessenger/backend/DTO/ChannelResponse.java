package org.instantmessenger.backend.DTO;

import java.util.List;

public record ChannelResponse(
        long id,
        String name,
        List<Long> memberIds
) {}
