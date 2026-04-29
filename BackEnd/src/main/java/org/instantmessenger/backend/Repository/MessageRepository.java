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

    public List<Message> findByChannelId(Long channelId) {
        return jdbc.query(
                "SELECT * FROM messages WHERE channel_id = ?",
                (rs, rowNum) -> {
                    Message m = new Message();
                    m.setId(rs.getLong("id"));
                    m.setContent(rs.getString("content"));
                    m.setUserId(rs.getLong("user_id"));
                    m.setChannelId(rs.getLong("channel_id"));
                    m.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    return m;
                },
                channelId
        );
    }

    public void save(Message message) {
        jdbc.update(
                "INSERT INTO messages (content, user_id, channel_id, timestamp) VALUES (?, ?, ?, ?)",
                message.getContent(),
                message.getUserId(),
                message.getChannelId(),
                Timestamp.valueOf(message.getTimestamp())
        );
    }
}