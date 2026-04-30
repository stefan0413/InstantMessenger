package org.instantmessenger.backend.Repository;

import org.instantmessenger.backend.DTO.MessageRequest;
import org.instantmessenger.backend.Model.Message;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    public Message getByIdOrElseThrow(long id){
        return getById(id).orElseThrow();
    }

    public Optional<Message> getById(long id){
        var message = jdbc.query("SELECT * FROM messages WHERE id = ?",
                ROW_MAPPER,
                id);

        return message.stream().findFirst();
    }

    public long save(MessageRequest request) {
        var keyholder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var ps = connection.prepareStatement(
                    "INSERT INTO messages (content, user_id, channel_id) VALUES (?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, request.content());
            ps.setLong(2, request.userId());
            ps.setLong(3, request.channelId());
            return ps;
        }, keyholder);

        return keyholder.getKey().longValue();
    }
}
