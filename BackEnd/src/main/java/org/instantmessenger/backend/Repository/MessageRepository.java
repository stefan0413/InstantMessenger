package org.instantmessenger.backend.Repository;

import org.instantmessenger.backend.DTO.MessageRequest;
import org.instantmessenger.backend.Model.Message;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MessageRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public MessageRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final MessageRowMapper ROW_MAPPER = new MessageRowMapper();

    public List<Message> findByChannelId(Long channelId) {
        return jdbc.query(
                "SELECT * FROM messages WHERE channel_id = :channelId",
                new MapSqlParameterSource("channelId", channelId),
                ROW_MAPPER
        );
    }

    public Message getByIdOrElseThrow(long id){
        return getById(id).orElseThrow();
    }

    public Optional<Message> getById(long id){
        var result = jdbc.query(
                "SELECT * FROM messages WHERE id = :id",
                new MapSqlParameterSource("id", id),
                ROW_MAPPER
        );

        return result.stream().findFirst();
    }

    public long save(MessageRequest request) {
        var sql = """
            INSERT INTO messages (content, user_id, channel_id)
            VALUES (:content, :userId, :channelId)
        """;

        var params = new MapSqlParameterSource()
                .addValue("content", request.content())
                .addValue("userId", request.userId())
                .addValue("channelId", request.channelId());

        var keyHolder = new GeneratedKeyHolder();

        jdbc.update(sql, params, keyHolder, new String[]{"id"});

        return keyHolder.getKey().longValue();
    }
}
