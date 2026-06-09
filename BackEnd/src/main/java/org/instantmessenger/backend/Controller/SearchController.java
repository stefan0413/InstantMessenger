package org.instantmessenger.backend.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.instantmessenger.backend.Model.Message;
import org.instantmessenger.backend.config.AuthenticatedUser;
import org.instantmessenger.backend.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final MessageService messageService;

    public SearchController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public List<Message> search(
            @RequestParam String query,
            @RequestParam Long channelId,
            @RequestParam(defaultValue = "100") int limit,
            HttpServletRequest request
    ) {
        return messageService.searchByChannel(channelId, query, AuthenticatedUser.from(request), limit);
    }
}
