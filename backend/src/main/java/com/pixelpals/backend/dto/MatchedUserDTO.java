package com.pixelpals.backend.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
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
    private boolean isOnline;
    private int compatibilityScore;
    private List<String> commonGames;
    private List<String> commonPlatforms;
    private String skillLevelForGame;
}
