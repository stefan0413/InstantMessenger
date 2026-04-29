package org.instantmessenger.backend.Repository;

import org.instantmessenger.backend.Model.Channel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChannelRepository {

    private final JdbcTemplate jdbc;

    public ChannelRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<String> findAllNames() {
        return jdbc.query(
                "SELECT name FROM channels",
                (rs, rowNum) -> rs.getString("name")
        );
    }

    private static final ChannelRowMapper ROW_MAPPER = new ChannelRowMapper();

    public List<Channel> findAll() {
        return jdbc.query("SELECT * FROM channels", ROW_MAPPER);
    }
}