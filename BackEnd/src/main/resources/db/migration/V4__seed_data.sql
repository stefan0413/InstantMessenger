INSERT INTO users (username, password)
VALUES ('ivan', 'test'),
       ('maria', 'test');

INSERT INTO channels (name)
VALUES ('general'),
       ('random');

INSERT INTO messages (content, user_id, channel_id, timestamp)
VALUES
    ('Hello!', 1, 1, NOW()),
    ('Hi there!', 2, 1, NOW());