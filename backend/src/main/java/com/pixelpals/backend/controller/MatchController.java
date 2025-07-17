package com.pixelpals.backend.controller;

import com.pixelpals.backend.dto.GameMatchRequestDTO;
import com.pixelpals.backend.dto.MatchDetailsDTO;
import com.pixelpals.backend.dto.MatchRequestDTO;
import com.pixelpals.backend.dto.MatchedUserDTO;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.service.MatchService;
import com.pixelpals.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final UserService userService;

    @PostMapping("/request")
    public ResponseEntity<?> requestGameMatch(@RequestBody GameMatchRequestDTO requestDTO, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String senderId = ((User) userDetails).getId();
            MatchDetailsDTO newMatch = matchService.requestGameMatch(senderId, requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(newMatch);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    // Modificato da @GetMapping a @PostMapping per supportare le richieste POST dal frontend
    @PostMapping("/find")
    public ResponseEntity<List<MatchedUserDTO>> findMatches(@RequestBody MatchRequestDTO request, @AuthenticationPrincipal UserDetails userDetails) {
        String currentUsername = userDetails.getUsername();
        List<MatchedUserDTO> matches = matchService.findMatches(request, currentUsername);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/pending-game-match")
    public ResponseEntity<List<MatchDetailsDTO>> getPendingGameMatches(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String userId = ((User) userDetails).getId();
            List<MatchDetailsDTO> pendingMatches = matchService.getPendingGameMatchesForUser(userId);
            return ResponseEntity.ok(pendingMatches);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @PutMapping("/{matchId}/accept")
    public ResponseEntity<?> acceptGameMatch(@PathVariable String matchId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String userId = ((User) userDetails).getId();

            // --- DEBUG LOGS AGGIUNTI ---
            System.out.println("DEBUG in MatchController.acceptGameMatch:");
            System.out.println("  matchId ricevuto da @PathVariable: " + matchId);
            System.out.println("  userId estratto da UserDetails: " + userId);
            // --- FINE DEBUG LOGS ---

            MatchDetailsDTO acceptedMatch = matchService.acceptGameMatch(userId, matchId);
            return ResponseEntity.ok(acceptedMatch);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{matchId}/decline")
    public ResponseEntity<?> declineGameMatch(@PathVariable String matchId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String userId = ((User) userDetails).getId();
            MatchDetailsDTO declinedMatch = matchService.declineGameMatch(userId, matchId);
            return ResponseEntity.ok(declinedMatch);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<MatchDetailsDTO> getMatchDetails(@PathVariable String matchId) {
        try {
            MatchDetailsDTO matchDetails = matchService.getMatchDetails(matchId);
            return ResponseEntity.ok(matchDetails);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
