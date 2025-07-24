package com.pixelpals.backend.mapper;
import com.pixelpals.backend.dto.GameDTO;
import com.pixelpals.backend.model.Game;
public class GameMapper {
    public static GameDTO toDTO(Game game) {
        if (game == null) {
            return null;
        }
        GameDTO dto = new GameDTO();
        dto.setId(game.getId());
        dto.setName(game.getName());
        dto.setGenre(game.getGenre());
        dto.setImageUrl(game.getImageUrl());
        return dto;
    }
    public static Game toEntity(GameDTO gameDTO) {
        if (gameDTO == null) {
            return null;
        }
        Game game = new Game();
        game.setId(gameDTO.getId());
        game.setName(gameDTO.getName());
        game.setGenre(gameDTO.getGenre());
        game.setImageUrl(gameDTO.getImageUrl());
        return game;
    }
}

