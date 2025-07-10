package com.pixelpals.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchedUserDTO {
    private String id;
    private String username;
    private String avatarUrl;
    private String bio;
    private int level;
    private double rating;
    private boolean online;
    private List<String> commonGames; // Giochi in comune
    private List<String> commonPlatforms; // Piattaforme in comune
    private String skillLevelForGame; // Livello di skill dell'utente per il gioco richiesto
    private double compatibilityScore; // Punteggio di compatibilità (0-100)
}