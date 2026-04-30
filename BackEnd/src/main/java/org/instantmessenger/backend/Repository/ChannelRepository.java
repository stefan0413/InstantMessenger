package org.instantmessenger.backend.Repository;

import org.instantmessenger.backend.Model.Channel;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChannelRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ChannelRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final ChannelRowMapper ROW_MAPPER = new ChannelRowMapper();

    public List<Channel> findAll() {
        return jdbc.query(
                "SELECT * FROM channels",
                ROW_MAPPER
        );
    }

    public boolean existsById(Long id) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM channels WHERE id = :id",
                new MapSqlParameterSource("id", id),
                Integer.class
        );

        return count != null && count > 0;
    }
}