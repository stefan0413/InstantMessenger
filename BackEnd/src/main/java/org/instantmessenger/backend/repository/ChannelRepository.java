package org.instantmessenger.backend.repository;

import org.instantmessenger.backend.model.Channel;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public List<Channel> findByMemberId(long userId) {
        return jdbc.query(
                """
                SELECT c.*
                FROM channels c
                JOIN channel_members cm ON cm.channel_id = c.id
                WHERE cm.user_id = :userId
                ORDER BY c.id DESC
                """,
                new MapSqlParameterSource("userId", userId),
                ROW_MAPPER
        );
    }

    public Map<Long, List<Long>> findMemberIdsForChannels(List<Long> channelIds) {
        if (channelIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<Long>> result = new LinkedHashMap<>();
        jdbc.query(
                "SELECT channel_id, user_id FROM channel_members WHERE channel_id IN (:channelIds) ORDER BY channel_id",
                new MapSqlParameterSource("channelIds", channelIds),
                (rs) -> {
                    long channelId = rs.getLong("channel_id");
                    long userId = rs.getLong("user_id");
                    result.computeIfAbsent(channelId, k -> new ArrayList<>()).add(userId);
                }
        );
        return result;
    }

    public List<Long> findMemberIdsForChannel(Long channelId) {
        return jdbc.query(
                "SELECT user_id FROM channel_members WHERE channel_id = :channelId",
                new MapSqlParameterSource("channelId", channelId),
                (rs, rowNum) -> rs.getLong("user_id")
        );
    }

    public Channel create(String name) {
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(
                "INSERT INTO channels (name) VALUES (:name)",
                new MapSqlParameterSource("name", name),
                keyHolder,
                new String[]{"id"});
        long id = keyHolder.getKey().longValue();
        return new Channel(id, name);
    }

    public void addMembers(long channelId, List<Long> userIds) {
        var params = userIds.stream()
                .map(userId -> new MapSqlParameterSource()
                        .addValue("channelId", channelId)
                        .addValue("userId", userId))
                .toArray(MapSqlParameterSource[]::new);

        jdbc.batchUpdate(
                "INSERT INTO channel_members (channel_id, user_id) VALUES (:channelId, :userId)",
                params
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

    public boolean isMember(long channelId, long userId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM channel_members WHERE channel_id = :channelId AND user_id = :userId",
                new MapSqlParameterSource()
                        .addValue("channelId", channelId)
                        .addValue("userId", userId),
                Integer.class
        );
        return count != null && count > 0;
    }
}
