package com.pixelpals.backend.controller;
import com.pixelpals.backend.dto.FriendDTO;
import com.pixelpals.backend.dto.FriendshipDTO;
import com.pixelpals.backend.dto.UserDTO;
import com.pixelpals.backend.enumeration.FriendshipStatus;
import com.pixelpals.backend.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;
    @PostMapping("/request/{receiverUsername}")
    public ResponseEntity<?> sendFriendRequest(@PathVariable String receiverUsername, Principal principal) {
        try {
            FriendshipDTO friendship = friendshipService.sendFriendRequest(principal.getName(), receiverUsername);
            return ResponseEntity.status(HttpStatus.CREATED).body(friendship);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
    @PutMapping("/accept/{requestId}")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable String requestId, Principal principal) {
        try {
            FriendshipDTO friendship = friendshipService.acceptFriendRequest(requestId, principal.getName());
            return ResponseEntity.ok(friendship);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
    @PutMapping("/reject/{requestId}")
    public ResponseEntity<?> rejectFriendRequest(@PathVariable String requestId, Principal principal) {
        try {
            FriendshipDTO friendship = friendshipService.rejectFriendRequest(requestId, principal.getName());
            return ResponseEntity.ok(friendship);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
    @DeleteMapping("/remove/{friendId}")
    public ResponseEntity<?> removeFriend(@PathVariable String friendId, Principal principal) {
        try {
            friendshipService.removeFriend(friendId, principal.getName());
            return ResponseEntity.ok(Map.of("message", "Amicizia rimossa con successo."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
    @GetMapping
    public ResponseEntity<List<UserDTO>> getFriends(Principal principal) {
        List<UserDTO> friends = friendshipService.getFriends(principal.getName());
        return ResponseEntity.ok(friends);
    }
    @GetMapping("/pending")
    public ResponseEntity<List<FriendshipDTO>> getPendingRequests(Principal principal) {
        List<FriendshipDTO> pendingRequests = friendshipService.getPendingRequests(principal.getName());
        return ResponseEntity.ok(pendingRequests);
    }
    @GetMapping("/sent")
    public ResponseEntity<List<FriendshipDTO>> getSentRequests(Principal principal) {
        List<FriendshipDTO> sentRequests = friendshipService.getSentRequests(principal.getName());
        return ResponseEntity.ok(sentRequests);
    }
    @GetMapping("/status/{otherUserId}")
    public ResponseEntity<Map<String, String>> getFriendshipStatusWithUser(
            @PathVariable String otherUserId,
            Principal principal) {
        try {
            String currentUsername = principal.getName();
            FriendshipStatus status = friendshipService.getFriendshipStatusBetweenUsers(currentUsername, otherUserId);
            return ResponseEntity.ok(Map.of("status", status.name()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Errore nel recupero dello stato di amicizia: " + e.getMessage()));
        }}
}
