package org.instantmessenger.backend.Controller;

import org.instantmessenger.backend.DTO.UserView;
import org.instantmessenger.backend.Repository.UserRepository;
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
            @RequestParam long excludeUserId,
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "25") int limit
    ) {
        return userRepository.search(query, excludeUserId, limit).stream()
                .map(user -> new UserView(user.id(), user.username(), user.email()))
                .toList();
    }
}
