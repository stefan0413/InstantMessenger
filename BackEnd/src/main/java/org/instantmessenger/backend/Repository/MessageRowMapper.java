package org.instantmessenger.backend.Repository;

import org.instantmessenger.backend.Model.Message;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MessageRowMapper implements RowMapper<Message> {

    @Override
    public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Message(
                rs.getLong("id"),
                rs.getString("content"),
                rs.getLong("user_id"),
                rs.getLong("channel_id"),
                rs.getTimestamp("timestamp").toLocalDateTime(),
                rs.getString("file_url"),
                rs.getString("file_name")
        );
    }
}
