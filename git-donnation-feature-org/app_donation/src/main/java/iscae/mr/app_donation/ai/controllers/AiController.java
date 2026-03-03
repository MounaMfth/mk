package iscae.mr.app_donation.ai.controllers;

import iscae.mr.app_donation.ai.services.AiAgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AiController {

    private final AiAgentService aiAgentService;

    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('USER','ORG','ADMIN')")
    public String chat(@RequestBody String message) {
        return aiAgentService.chat(message);
    }
}
