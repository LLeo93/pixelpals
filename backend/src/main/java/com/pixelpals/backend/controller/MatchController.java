package com.pixelpals.backend.controller;

import com.pixelpals.backend.dto.MatchRequestDTO;
import com.pixelpals.backend.dto.MatchResponseDTO;
import com.pixelpals.backend.dto.MatchedUserDTO;
import com.pixelpals.backend.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping("/find")
    public ResponseEntity<?> findMatches(@RequestBody MatchRequestDTO request, Principal principal) {
        try {
            List<MatchedUserDTO> matchedUsers = matchService.findMatches(request, principal.getName());
            if (matchedUsers.isEmpty()) {
                return ResponseEntity.ok(MatchResponseDTO.builder()
                        .message("Nessun PixelPal trovato con i criteri specificati.")
                        .matchedUsers(matchedUsers)
                        .build());
            }
            return ResponseEntity.ok(MatchResponseDTO.builder()
                    .message("Match trovati con successo!")
                    .matchedUsers(matchedUsers)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Errore durante la ricerca di match: " + e.getMessage()));
        }
    }
}
