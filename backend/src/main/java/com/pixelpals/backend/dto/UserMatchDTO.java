package com.pixelpals.backend.dto;

import com.pixelpals.backend.model.User;
import java.util.List;
import java.util.stream.Collectors;

public class UserMatchDTO {
    private String username;
    private int level;  // ti avevo detto String, ma nel modello User è int
    private List<String> preferredGames;

    public UserMatchDTO(String username, int level, List<String> preferredGames) {
        this.username = username;
        this.level = level;
        this.preferredGames = preferredGames;
    }

    public String getUsername() {
        return username;
    }

    public int getLevel() {
        return level;
    }

    public List<String> getPreferredGames() {
        return preferredGames;
    }

    // Metodo statico per creare DTO da User
    public static UserMatchDTO fromUser(User user) {
        List<String> gameNames = user.getPreferredGames().stream()
                .map(game -> game.getName())  // supponendo che Game abbia getName()
                .collect(Collectors.toList());

        return new UserMatchDTO(
                user.getUsername(),
                user.getLevel(),
                gameNames
        );
    }
}
