package com.pixelpals.backend.service;
import com.pixelpals.backend.model.Game;
import com.pixelpals.backend.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }
    public List<Game> searchGames(String searchTerm) {
        return gameRepository.findByNameContainingIgnoreCase(searchTerm);
    }
    public java.util.Optional<Game> getGameById(String id) {
        return gameRepository.findById(id);
    }
}

