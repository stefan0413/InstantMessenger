package org.instantmessenger.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.instantmessenger.backend.dto.UserView;
import org.instantmessenger.backend.repository.UserRepository;
import org.instantmessenger.backend.config.AuthenticatedUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<UserView> search(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "25") int limit,
            HttpServletRequest request
    ) {
        return userRepository.search(query, AuthenticatedUser.from(request), limit).stream()
                .map(user -> new UserView(user.id(), user.username(), user.email()))
                .toList();
    }
}
