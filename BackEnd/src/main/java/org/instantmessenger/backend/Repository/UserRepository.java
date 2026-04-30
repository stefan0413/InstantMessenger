package org.instantmessenger.backend.Repository;

import org.instantmessenger.backend.Model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class UserRepository {

    private static final RowMapper<User> ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password_hash")
    );

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<User> findByEmail(String email) {
        return jdbc.query(
                "SELECT id, username, email, password_hash FROM users WHERE LOWER(email) = LOWER(?)",
                ROW_MAPPER,
                email
        ).stream().findFirst();
    }

    public Optional<User> findById(long id) {
        return jdbc.query(
                "SELECT id, username, email, password_hash FROM users WHERE id = ?",
                ROW_MAPPER,
                id
        ).stream().findFirst();
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?)",
                Integer.class,
                username
        );
        return count != null && count > 0;
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)",
                Integer.class,
                email
        );
        return count != null && count > 0;
    }

    public User create(String username, String email, String passwordHash) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("User id was not generated");
        }

        return new User(key.longValue(), username, email, passwordHash);
    }
}
