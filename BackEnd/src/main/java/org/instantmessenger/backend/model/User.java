package org.instantmessenger.backend.model;

public record User(long id,
                   String username,
                   String email,
                   String passwordHash) {
}
