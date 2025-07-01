package com.pixelpals.backend.controller;

import com.pixelpals.backend.dto.GameDTO;
import com.pixelpals.backend.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping
    public List<GameDTO> getAllGames() {
        return gameService.getAllGames()
                .stream()
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
