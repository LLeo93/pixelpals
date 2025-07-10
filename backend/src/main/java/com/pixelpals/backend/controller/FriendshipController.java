package com.pixelpals.backend.controller;

import com.pixelpals.backend.dto.FriendshipDTO;
import com.pixelpals.backend.dto.UserDTO;
import com.pixelpals.backend.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {
    private final FriendshipService friendshipService;
    // Invia una richiesta di amicizia
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

    // Accetta una richiesta di amicizia
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

    // Rifiuta una richiesta di amicizia
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

    // Rimuovi un amico (elimina la relazione ACCEPTED)
    @DeleteMapping("/remove/{friendId}") // friendId è l'ID dell'utente amico, non l'ID della friendship
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

    // Ottieni la lista degli amici dell'utente corrente
    @GetMapping
    public ResponseEntity<List<UserDTO>> getFriends(Principal principal) {
        List<UserDTO> friends = friendshipService.getFriends(principal.getName());
        return ResponseEntity.ok(friends);
    }

    // Ottieni le richieste di amicizia in sospeso ricevute dall'utente corrente
    @GetMapping("/pending")
    public ResponseEntity<List<FriendshipDTO>> getPendingRequests(Principal principal) {
        List<FriendshipDTO> pendingRequests = friendshipService.getPendingRequests(principal.getName());
        return ResponseEntity.ok(pendingRequests);
    }

    // Ottieni le richieste di amicizia in sospeso inviate dall'utente corrente
    @GetMapping("/sent")
    public ResponseEntity<List<FriendshipDTO>> getSentRequests(Principal principal) {
        List<FriendshipDTO> sentRequests = friendshipService.getSentRequests(principal.getName());
        return ResponseEntity.ok(sentRequests);
    }
}
