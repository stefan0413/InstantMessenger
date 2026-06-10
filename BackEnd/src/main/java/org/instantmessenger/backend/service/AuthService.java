package org.instantmessenger.backend.service;

import org.instantmessenger.backend.dto.AuthResponse;
import org.instantmessenger.backend.dto.LoginRequest;
import org.instantmessenger.backend.dto.RegisterRequest;
import org.instantmessenger.backend.model.User;
import org.instantmessenger.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();

        if (!request.password().equals(request.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = userRepository.create(username, email, passwordHash);
        return AuthResponse.from(user, jwtService.createToken(user.id(), user.email()));
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return AuthResponse.from(user, jwtService.createToken(user.id(), user.email()));
    }
}
