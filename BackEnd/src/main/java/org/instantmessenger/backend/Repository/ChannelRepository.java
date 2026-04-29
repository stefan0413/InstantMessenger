package org.instantmessenger.backend.Repository;

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
}