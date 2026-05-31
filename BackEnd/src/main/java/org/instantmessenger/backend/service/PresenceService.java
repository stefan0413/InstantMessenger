package org.instantmessenger.backend.service;

import org.instantmessenger.backend.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {

    private final UserRepository userRepository;
    private final ConcurrentHashMap<String, Long> sessionToUser = new ConcurrentHashMap<>();

    public PresenceService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void register(String sessionId, long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        sessionToUser.put(sessionId, userId);
    }

    public Optional<Long> unregister(String sessionId) {
        return Optional.ofNullable(sessionToUser.remove(sessionId));
    }

    public Set<Long> getOnlineUserIds() {
        return Set.copyOf(sessionToUser.values());
    }
}
