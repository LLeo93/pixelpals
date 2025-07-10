package com.pixelpals.backend.service;

import com.pixelpals.backend.dto.MatchRequestDTO;
import com.pixelpals.backend.dto.MatchedUserDTO;
import com.pixelpals.backend.enumeration.SkillLevel;
import com.pixelpals.backend.model.Game;
import com.pixelpals.backend.model.Platform;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.repository.GameRepository;
import com.pixelpals.backend.repository.PlatformRepository;
import com.pixelpals.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final PlatformRepository platformRepository; // Necessario per filtrare le piattaforme

    public List<MatchedUserDTO> findMatches(MatchRequestDTO request, String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Utente corrente non trovato."));

        // Recupera il gioco e la piattaforma richiesti (se specificati)
        Optional<Game> requestedGame = request.getGameName() != null && !request.getGameName().isEmpty()
                ? gameRepository.findByName(request.getGameName())
                : Optional.empty();

        Optional<Platform> requestedPlatform = request.getPlatformName() != null && !request.getPlatformName().isEmpty()
                ? platformRepository.findByName(request.getPlatformName())
                : Optional.empty();

        // Filtra tutti gli utenti tranne l'utente corrente
        List<User> allOtherUsers = userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .collect(Collectors.toList());

        List<MatchedUserDTO> matches = new ArrayList<>();

        for (User otherUser : allOtherUsers) {
            double compatibilityScore = 0.0;
            List<String> commonGames = new ArrayList<>();
            List<String> commonPlatforms = new ArrayList<>();
            String skillLevelForGame = null;

            // 1. Compatibilità per giochi
            if (currentUser.getPreferredGames() != null && otherUser.getPreferredGames() != null) {
                Set<String> currentUserGames = currentUser.getPreferredGames().stream()
                        .map(Game::getName)
                        .collect(Collectors.toSet());
                Set<String> otherUserGames = otherUser.getPreferredGames().stream()
                        .map(Game::getName)
                        .collect(Collectors.toSet());

                commonGames = currentUserGames.stream()
                        .filter(otherUserGames::contains)
                        .collect(Collectors.toList());

                if (!commonGames.isEmpty()) {
                    compatibilityScore += commonGames.size() * 10; // Punti per giochi in comune
                }
            }

            // 2. Compatibilità per piattaforme
            if (currentUser.getPlatforms() != null && otherUser.getPlatforms() != null) {
                Set<String> currentUserPlatforms = currentUser.getPlatforms().stream()
                        .map(Platform::getName)
                        .collect(Collectors.toSet());
                Set<String> otherUserPlatforms = otherUser.getPlatforms().stream()
                        .map(Platform::getName)
                        .collect(Collectors.toSet());

                commonPlatforms = currentUserPlatforms.stream()
                        .filter(otherUserPlatforms::contains)
                        .collect(Collectors.toList());

                if (!commonPlatforms.isEmpty()) {
                    compatibilityScore += commonPlatforms.size() * 5; // Punti per piattaforme in comune
                }
            }

            // 3. Compatibilità per skill level (se un gioco è richiesto e comune)
            if (requestedGame.isPresent() && commonGames.contains(requestedGame.get().getName())) {
                String reqGameName = requestedGame.get().getName();
                SkillLevel currentUserSkill = currentUser.getSkillLevelMap() != null ? currentUser.getSkillLevelMap().get(reqGameName) : null;
                SkillLevel otherUserSkill = otherUser.getSkillLevelMap() != null ? otherUser.getSkillLevelMap().get(reqGameName) : null;

                if (otherUserSkill != null) {
                    skillLevelForGame = otherUserSkill.name(); // Registra la skill dell'altro utente

                    if (currentUserSkill != null && otherUserSkill != null) {
                        // Calcola la differenza di skill level (es. 0 per match perfetto, 3 per max differenza)
                        int skillDifference = Math.abs(currentUserSkill.ordinal() - otherUserSkill.ordinal());
                        compatibilityScore += (3 - skillDifference) * 15; // Più vicini, più punti
                    }
                }
            }

            // 4. Filtro per gioco e piattaforma specifici se richiesti
            boolean passesGameFilter = !requestedGame.isPresent() || commonGames.contains(requestedGame.get().getName());
            boolean passesPlatformFilter = !requestedPlatform.isPresent() || commonPlatforms.contains(requestedPlatform.get().getName());

            if (passesGameFilter && passesPlatformFilter) {
                matches.add(MatchedUserDTO.builder()
                        .id(otherUser.getId())
                        .username(otherUser.getUsername())
                        .avatarUrl(otherUser.getAvatarUrl())
                        .bio(otherUser.getBio())
                        .level(otherUser.getLevel())
                        .rating(otherUser.getRating())
                        .online(otherUser.isOnline())
                        .commonGames(commonGames)
                        .commonPlatforms(commonPlatforms)
                        .skillLevelForGame(skillLevelForGame)
                        .compatibilityScore(compatibilityScore)
                        .build());
            }
        }

        // Ordina i risultati per punteggio di compatibilità (decrescente)
        matches.sort(Comparator.comparingDouble(MatchedUserDTO::getCompatibilityScore).reversed());

        // Limita il numero di risultati se maxResults è specificato
        if (request.getMaxResults() > 0 && matches.size() > request.getMaxResults()) {
            return matches.subList(0, request.getMaxResults());
        }

        return matches;
    }
}
