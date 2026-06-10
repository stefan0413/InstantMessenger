package org.instantmessenger.backend.dto;

import org.instantmessenger.backend.model.User;

public record AuthResponse(UserView user, String token) {
    public static AuthResponse from(User user, String token) {
        return new AuthResponse(new UserView(user.id(), user.username(), user.email()), token);
    }
}
