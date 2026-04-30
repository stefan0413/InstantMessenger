ALTER TABLE users
    ADD COLUMN email VARCHAR(255);

ALTER TABLE users
    ADD COLUMN password_hash VARCHAR(255);

UPDATE users
SET email = CONCAT(username, '-', id, '@local.invalid'),
    password_hash = '$2a$10$MNDq6WcBi26Bc4mV5kwIxOReSCipryKMZDNOH3icjdMn9xGCBmfPS'
WHERE email IS NULL
   OR password_hash IS NULL;

ALTER TABLE users
    ALTER COLUMN email SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN password_hash SET NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT users_email_unique UNIQUE (email);
