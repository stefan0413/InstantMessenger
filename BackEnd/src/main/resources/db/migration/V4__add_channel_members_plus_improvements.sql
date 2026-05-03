CREATE TABLE channel_members (
    channel_id INTEGER NOT NULL REFERENCES channels(id) ON DELETE CASCADE,
    user_id    INTEGER NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    PRIMARY KEY (channel_id, user_id)
);

ALTER TABLE channels
    ADD CONSTRAINT channels_name_unique UNIQUE (name);

CREATE INDEX idx_channel_members_user_id ON channel_members(user_id);

