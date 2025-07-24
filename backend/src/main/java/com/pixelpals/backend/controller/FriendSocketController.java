package com.pixelpals.backend.controller;

import com.pixelpals.backend.dto.FriendDTO;
import com.pixelpals.backend.model.Friendship;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.service.FriendshipService;
import com.pixelpals.backend.service.UserService;
import com.pixelpals.backend.WebSocket.UserSessionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FriendSocketController {

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserSessionRegistry userSessionRegistry;

    @MessageMapping("/friend/list")
    public void sendFriendList(Principal principal) {
        String username = principal.getName();
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Friendship> friendships = friendshipService.getAcceptedFriendships(user);

        List<FriendDTO> friendList = friendships.stream()
                .map(f -> {
                    User friend = f.getSender().getId().equals(user.getId()) ? f.getReceiver() : f.getSender();
                    boolean isFriendOnline = userSessionRegistry.isUserOnline(friend.getId());
                    return new FriendDTO(friend.getId(), friend.getUsername(), isFriendOnline, friend.getAvatarUrl());
                })
                .collect(Collectors.toList());

        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/friends",
                friendList
        );
    }
}
