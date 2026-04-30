package org.instantmessenger.backend.Repository;

import org.instantmessenger.backend.Model.User;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
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

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("User id was not generated");
        }

        return new User(key.longValue(), username, email, passwordHash);
    }
}
