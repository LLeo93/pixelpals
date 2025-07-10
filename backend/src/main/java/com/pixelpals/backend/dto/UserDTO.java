package com.pixelpals.backend.dto;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UserDTO {
    private String id;
    private String username;
    private String email;
    private String avatarUrl;
    private String bio;
    private int level;
    private double rating;
    private boolean online;
    private boolean verified;
    private List<GameDTO> preferredGames; // Lista di GameDTO
    private List<PlatformDTO> platforms; // Lista di PlatformDTO
    private Map<String, String> skillLevelMap; // Mappa GameName -> SkillLevel (stringa)
}
