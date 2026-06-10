package org.instantmessenger.backend.repository;

import org.instantmessenger.backend.model.Channel;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ChannelRowMapper implements RowMapper<Channel> {

    @Override
    public Channel mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Channel(
                rs.getLong("id"),
                rs.getString("name")
        );
    }
}
