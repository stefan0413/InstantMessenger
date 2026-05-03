package org.instantmessenger.backend.DTO;

import org.instantmessenger.backend.Model.User;

public record AuthResponse(UserView user, String token) {
    public static AuthResponse from(User user, String token) {
        return new AuthResponse(new UserView(user.id(), user.username(), user.email()), token);
    }
}
