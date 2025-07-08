package com.pixelpals.backend.controller;

import com.pixelpals.backend.dto.UserMatchDTO;
import com.pixelpals.backend.model.TimeSlot;
import com.pixelpals.backend.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/match")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    // 1. Trova match generici (basato sui giochi in comune)
    @GetMapping("/find")
    public ResponseEntity<List<UserMatchDTO>> findGeneralMatches(Principal principal) {
        return ResponseEntity.ok(matchService.findMatches(principal.getName()));
    }

    // 2. Trova match per gioco specifico
    @GetMapping("/find/byGame")
    public ResponseEntity<List<UserMatchDTO>> findMatchesByGame(@RequestParam String game, Principal principal) {
        return ResponseEntity.ok(matchService.findMatchesByGame(principal.getName(), game));
    }

    // 3. Trova match per skill compatibili
    @GetMapping("/find/bySkill")
    public ResponseEntity<List<UserMatchDTO>> findMatchesBySkillLevel(Principal principal) {
        return ResponseEntity.ok(matchService.findMatchesBySkillLevel(principal.getName()));
    }

    // 4. Trova match combinati: giochi, skill e disponibilità
    @GetMapping("/find/combined")
    public ResponseEntity<List<UserMatchDTO>> findCombinedMatches(Principal principal) {
        return ResponseEntity.ok(matchService.findCombinedMatches(principal.getName()));
    }
}
