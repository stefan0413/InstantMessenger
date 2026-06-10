package org.instantmessenger.backend.model;

import java.time.LocalDateTime;

public record Message(long id,
                      String content,
                      long userId,
                      long channelId,
                      LocalDateTime time,
                      String fileUrl,
                      String fileName) {
}