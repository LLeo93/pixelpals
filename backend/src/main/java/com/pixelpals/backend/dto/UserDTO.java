package com.pixelpals.backend.dto;
import com.pixelpals.backend.model.Badge;
import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.Objects;
@Data
public class UserDTO {
    private String id;
    private String username;
    private String email;
    private String role;
    private String avatarUrl;
    private String bio;
    private Integer level;
    private double rating;
    private boolean isOnline;
    private boolean verified;
    private int matchesPlayed;
    private int numberOfRatings;
    private List<GameDTO> preferredGames;
    private List<PlatformDTO> platforms;
    private Map<String, String> skillLevelMap;
    private List<Badge> badges;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return Objects.equals(id, userDTO.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
