package org.instantmessenger.backend.Repository;

import org.instantmessenger.backend.Model.Channel;
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
