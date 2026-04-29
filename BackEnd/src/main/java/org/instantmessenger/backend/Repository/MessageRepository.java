package org.instantmessenger.backend.Repository;

import org.instantmessenger.backend.Model.Message;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class MessageRepository {

    private final JdbcTemplate jdbc;

    public MessageRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final MessageRowMapper ROW_MAPPER = new MessageRowMapper();

    public List<Message> findByChannelId(Long channelId) {
        return jdbc.query(
                "SELECT * FROM messages WHERE channel_id = ?",
                ROW_MAPPER,
                channelId
        );
    }

    public void save(Message message) {
        jdbc.update(
                "INSERT INTO messages (content, user_id, channel_id, time) VALUES (?, ?, ?, ?)",
                message.content(),
                message.userId(),
                message.channelId(),
                Timestamp.valueOf(message.time())
        );
    }
}