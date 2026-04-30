package org.instantmessenger.backend.Model;

public record User(long id, String username, String email, String passwordHash) {
}
