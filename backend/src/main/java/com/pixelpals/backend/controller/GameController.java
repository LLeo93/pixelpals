package com.pixelpals.backend.controller;

import com.pixelpals.backend.dto.GameDTO;
import com.pixelpals.backend.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;
    @GetMapping
    public List<GameDTO> getAllGames(@RequestParam(required = false) String search) {
        List<com.pixelpals.backend.model.Game> games;
        if (search != null && !search.trim().isEmpty()) {
            games = gameService.searchGames(search);
        } else {
            games = gameService.getAllGames();
        }
        return games.stream()
                .map(game -> {
                    GameDTO dto = new GameDTO();
                    dto.setId(game.getId());
                    dto.setName(game.getName());
                    dto.setGenre(game.getGenre());
                    dto.setImageUrl(game.getImageUrl());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}

