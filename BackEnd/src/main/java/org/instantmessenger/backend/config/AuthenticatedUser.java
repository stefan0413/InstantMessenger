package org.instantmessenger.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

public final class AuthenticatedUser {

    public static final String ATTRIBUTE = "authenticatedUserId";

    private AuthenticatedUser() {
    }

    public static long from(HttpServletRequest request) {
        Object userId = request.getAttribute(ATTRIBUTE);
        if (userId instanceof Long value) {
            return value;
        }
        throw new IllegalStateException("Authenticated user is missing");
    }

    public static long from(SimpMessageHeaderAccessor accessor) {
        var attributes = accessor.getSessionAttributes();
        Object userId = attributes != null ? attributes.get(ATTRIBUTE) : null;
        if (userId instanceof Long value) {
            return value;
        }
        throw new IllegalStateException("Authenticated user is missing");
    }
}
