package org.instantmessenger.backend.repository;

import org.instantmessenger.backend.model.User;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private static final UserRowMapper ROW_MAPPER = new UserRowMapper();

    public UserRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<User> findAll() {
        return jdbc.query("SELECT * FROM users", ROW_MAPPER);
    }

    public List<User> search(String query, long excludeUserId, int limit) {
        String normalizedQuery = query == null ? "" : query.trim();
        int cappedLimit = Math.max(1, Math.min(limit, 50));

        var params = new MapSqlParameterSource()
                .addValue("excludeUserId", excludeUserId)
                .addValue("query", "%" + normalizedQuery.toLowerCase() + "%")
                .addValue("limit", cappedLimit);

        if (normalizedQuery.isBlank()) {
            return jdbc.query(
                    """
                    SELECT *
                    FROM users
                    WHERE id <> :excludeUserId
                    ORDER BY username
                    LIMIT :limit
                    """,
                    params,
                    ROW_MAPPER
            );
        }

        return jdbc.query(
                """
                SELECT *
                FROM users
                WHERE id <> :excludeUserId
                  AND (LOWER(username) LIKE :query OR LOWER(email) LIKE :query)
                ORDER BY username
                LIMIT :limit
                """,
                params,
                ROW_MAPPER
        );
    }

    public Map<Long, List<User>> findByChannelIds(List<Long> channelIds) {
        if (channelIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<User>> result = new LinkedHashMap<>();
        jdbc.query(
                """
                SELECT cm.channel_id, u.*
                FROM channel_members cm
                JOIN users u ON u.id = cm.user_id
                WHERE cm.channel_id IN (:channelIds)
                ORDER BY cm.channel_id, u.username
                """,
                new MapSqlParameterSource("channelIds", channelIds),
                rs -> {
                    long channelId = rs.getLong("channel_id");
                    var user = ROW_MAPPER.mapRow(rs, rs.getRow());
                    result.computeIfAbsent(channelId, ignored -> new ArrayList<>()).add(user);
                }
        );
        return result;
    }

    public Optional<User> findById(Long id) {
        return jdbc.query(
                "SELECT * FROM users WHERE id = :id",
                new MapSqlParameterSource("id", id),
                ROW_MAPPER
        ).stream().findFirst();
    }

    public Optional<User> findByUsername(String username) {
        return jdbc.query(
                "SELECT * FROM users WHERE username = :username",
                new MapSqlParameterSource("username", username),
                ROW_MAPPER
        ).stream().findFirst();
    }

    public Optional<User> findByEmail(String email) {
        return jdbc.query(
                "SELECT * FROM users WHERE LOWER(email) = LOWER(:email)",
                new MapSqlParameterSource("email", email),
                ROW_MAPPER
        ).stream().findFirst();
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(:username)",
                new MapSqlParameterSource("username", username),
                Integer.class
        );
        return count != null && count > 0;
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(:email)",
                new MapSqlParameterSource("email", email),
                Integer.class
        );
        return count != null && count > 0;
    }

    public boolean existsById(long id) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = :id",
                new MapSqlParameterSource("id", id),
                Integer.class
        );
        return count != null && count > 0;
    }

    public User create(String username, String email, String passwordHash) {
        var params = new MapSqlParameterSource()
                .addValue("username", username)
                .addValue("email", email)
                .addValue("passwordHash", passwordHash);

        var keyHolder = new GeneratedKeyHolder();

        jdbc.update(
                "INSERT INTO users (username, email, password_hash) VALUES (:username, :email, :passwordHash)",
                params,
                keyHolder,
                new String[]{"id"}
        );

        long id = keyHolder.getKey().longValue();
        return new User(id, username, email, passwordHash, false);
    }

    public void saveVerificationToken(long userId, String token) {
        jdbc.update(
                "UPDATE users SET verification_token = :token WHERE id = :id",
                new MapSqlParameterSource().addValue("token", token).addValue("id", userId)
        );
    }

    public Optional<User> findByVerificationToken(String token) {
        return jdbc.query(
                "SELECT * FROM users WHERE verification_token = :token",
                new MapSqlParameterSource("token", token),
                ROW_MAPPER
        ).stream().findFirst();
    }

    public void markEmailVerified(long userId) {
        jdbc.update(
                "UPDATE users SET email_verified = TRUE, verification_token = NULL WHERE id = :id",
                new MapSqlParameterSource("id", userId)
        );
    }
}
