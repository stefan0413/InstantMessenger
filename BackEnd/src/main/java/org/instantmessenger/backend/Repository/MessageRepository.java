package org.instantmessenger.backend.Repository;

import org.instantmessenger.backend.DTO.MessageRequest;
import org.instantmessenger.backend.Model.Message;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class MessageRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public MessageRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final MessageRowMapper ROW_MAPPER = new MessageRowMapper();

    public List<Message> findByChannelId(Long channelId, int limit, Long before) {
        var params = new MapSqlParameterSource()
                .addValue("channelId", channelId)
                .addValue("limit", limit);

        String sql;
        if (before != null) {
            sql = "SELECT * FROM messages WHERE channel_id = :channelId AND id < :before ORDER BY id DESC LIMIT :limit";
            params.addValue("before", before);
        } else {
            sql = "SELECT * FROM messages WHERE channel_id = :channelId ORDER BY id DESC LIMIT :limit";
        }

        var result = new ArrayList<>(jdbc.query(sql, params, ROW_MAPPER));
        Collections.reverse(result);
        return result;
    }

    public List<Message> searchByChannel(Long channelId, String query, int limit) {
        var params = new MapSqlParameterSource()
                .addValue("channelId", channelId)
                .addValue("pattern", "%" + escapeLike(query) + "%")
                .addValue("limit", limit);

        var sql = """
            SELECT * FROM messages
            WHERE channel_id = :channelId
              AND content ILIKE :pattern ESCAPE '\\'
            ORDER BY id DESC
            LIMIT :limit
        """;

        var result = new ArrayList<>(jdbc.query(sql, params, ROW_MAPPER));
        Collections.reverse(result);
        return result;
    }

    private static String escapeLike(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    public Message getByIdOrElseThrow(long id) {
        return getById(id).orElseThrow(() -> new IllegalArgumentException("Message not found: " + id));
    }

    public Optional<Message> getById(long id) {
        var result = jdbc.query(
                "SELECT * FROM messages WHERE id = :id",
                new MapSqlParameterSource("id", id),
                ROW_MAPPER
        );
        return result.stream().findFirst();
    }

    public long save(MessageRequest request, long userId) {
        var sql = """
            INSERT INTO messages (content, user_id, channel_id, file_url, file_name)
            VALUES (:content, :userId, :channelId, :fileUrl, :fileName)
        """;

        var params = new MapSqlParameterSource()
                .addValue("content", request.content())
                .addValue("userId", userId)
                .addValue("channelId", request.channelId())
                .addValue("fileUrl", request.fileUrl())
                .addValue("fileName", request.fileName());

        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params, keyHolder, new String[]{"id"});
        return keyHolder.getKey().longValue();
    }

}
