CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE channels (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(100) NOT NULL
);

CREATE TABLE messages (
                          id SERIAL PRIMARY KEY,
                          content TEXT NOT NULL,
                          user_id INTEGER NOT NULL,
                          channel_id INTEGER NOT NULL,
                          timestamp TIMESTAMP NOT NULL,

                          FOREIGN KEY (user_id) REFERENCES users(id),
                          FOREIGN KEY (channel_id) REFERENCES channels(id)
);