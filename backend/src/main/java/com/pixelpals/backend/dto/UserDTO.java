package com.pixelpals.backend.dto;
import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.Objects;
@Data
public class UserDTO {
    private String id;
    private String username;
    private String email;
    private String role; // Aggiunto il campo ruolo
    private String avatarUrl;
    private String bio;
    private Integer level;
    private double rating;
    private boolean isOnline; // Tipo primitivo boolean
    private boolean verified; // Tipo primitivo boolean
    private List<GameDTO> preferredGames; // Lista di GameDTO
    private List<PlatformDTO> platforms; // Lista di PlatformDTO
    private Map<String, String> skillLevelMap; // Mappa GameName -> SkillLevel (stringa)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return Objects.equals(id, userDTO.id); // Confronta solo per ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Genera hash solo per ID
    }
}
