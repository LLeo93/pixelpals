package com.pixelpals.backend.mapper;
import com.pixelpals.backend.dto.GameDTO;
import com.pixelpals.backend.model.Game; // Assicurati che il tuo modello Game esista

public class GameMapper {

    /**
     * Converte un oggetto Game in GameDTO.
     * @param game L'oggetto Game da convertire.
     * @return Il GameDTO risultante, o null se l'input è null.
     */
    public static GameDTO toDTO(Game game) {
        if (game == null) {
            return null;
        }
        GameDTO dto = new GameDTO();
        dto.setId(game.getId());
        dto.setName(game.getName());
        dto.setGenre(game.getGenre());
        dto.setImageUrl(game.getImageUrl()); // Mappa il campo imageUrl
        return dto;
    }

    /**
     * Converte un oggetto GameDTO in Game.
     * @param gameDTO L'oggetto GameDTO da convertire.
     * @return Il modello Game risultante, o null se l'input è null.
     */
    public static Game toEntity(GameDTO gameDTO) {
        if (gameDTO == null) {
            return null;
        }
        Game game = new Game();
        game.setId(gameDTO.getId());
        game.setName(gameDTO.getName());
        game.setGenre(gameDTO.getGenre());
        game.setImageUrl(gameDTO.getImageUrl()); // Mappa il campo imageUrl
        return game;
    }
}

