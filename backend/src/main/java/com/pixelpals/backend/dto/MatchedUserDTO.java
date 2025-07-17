package com.pixelpals.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchedUserDTO {
    private String id;
    private String username;
    private int level;
    private double rating;
    private String avatarUrl;
    private boolean isOnline; // Assicurati che il nome del campo sia corretto
    private int compatibilityScore;
    private List<String> commonGames;
    private List<String> commonPlatforms;
    private String skillLevelForGame; // Stringa per il livello di skill
    // Potresti avere altri campi qui
}
