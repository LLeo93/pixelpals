package com.pixelpals.backend.controller;
import com.pixelpals.backend.repository.OnlineUserRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Set;
@RestController
@RequestMapping("/api/online")
@RequiredArgsConstructor
public class OnlineStatusController {
    private final OnlineUserRegistry onlineUserRegistry;
    @GetMapping("/users")
    public Set<String> getOnlineUsers() {
        return onlineUserRegistry.getOnlineUsers();
    }
}