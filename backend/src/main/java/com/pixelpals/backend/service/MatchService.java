package com.pixelpals.backend.service;

import com.pixelpals.backend.dto.GameMatchRequestDTO;
import com.pixelpals.backend.dto.MatchDetailsDTO;
import com.pixelpals.backend.dto.MatchRequestDTO;
import com.pixelpals.backend.dto.MatchedUserDTO;
import com.pixelpals.backend.dto.RatingRequestDTO;
import com.pixelpals.backend.model.Game;
import com.pixelpals.backend.model.Match;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.enumeration.MatchStatus;
import com.pixelpals.backend.repository.GameRepository;
import com.pixelpals.backend.repository.MatchRepository;
import com.pixelpals.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final BadgeService badgeService;

    public List<MatchedUserDTO> findMatches(MatchRequestDTO request, String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found."));

        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .map(user -> {
                    int score = 0;

                    if (request.getGameName() != null && !request.getGameName().isEmpty()) {
                        if (user.getPreferredGames().stream().anyMatch(g -> g.getName().equals(request.getGameName()))) {
                            score += 50;
                        }
                    }
                    if (request.getPlatformName() != null && !request.getPlatformName().isEmpty()) {
                        if (user.getPlatforms().stream().anyMatch(p -> p.getName().equals(request.getPlatformName()))) {
                            score += 30;
                        }
                    }
                    if (request.getSkillLevel() != null && !request.getSkillLevel().isEmpty()) {
                        String userSkill = user.getSkillLevelMap().get(request.getGameName()) != null ?
                                user.getSkillLevelMap().get(request.getGameName()).name() : null;
                        if (userSkill != null && userSkill.equals(request.getSkillLevel())) {
                            score += 20;
                        }
                    }

                    List<String> commonGames = currentUser.getPreferredGames().stream()
                            .filter(g -> user.getPreferredGames().contains(g))
                            .map(Game::getName)
                            .collect(Collectors.toList());

                    List<String> commonPlatforms = currentUser.getPlatforms().stream()
                            .filter(p -> user.getPlatforms().contains(p))
                            .map(platform -> platform.getName())
                            .collect(Collectors.toList());

                    String skillLevelForGame = null;
                    if (request.getGameName() != null && !request.getGameName().isEmpty()) {
                        skillLevelForGame = user.getSkillLevelMap().get(request.getGameName()) != null ?
                                user.getSkillLevelMap().get(request.getGameName()).name() : null;
                    }

                    return MatchedUserDTO.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .level(user.getLevel())
                            .rating(user.getRating())
                            .avatarUrl(user.getAvatarUrl())
                            .isOnline(user.isOnline())
                            .compatibilityScore(score)
                            .commonGames(commonGames)
                            .commonPlatforms(commonPlatforms)
                            .skillLevelForGame(skillLevelForGame)
                            .build();
                })
                .filter(matchedUser -> matchedUser.getCompatibilityScore() > 0)
                .limit(request.getMaxResults())
                .collect(Collectors.toList());
    }

    public MatchDetailsDTO requestGameMatch(String senderId, GameMatchRequestDTO requestDTO) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found."));
        User receiver = userRepository.findById(requestDTO.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found."));
        Game game = gameRepository.findById(requestDTO.getGameId())
                .orElseThrow(() -> new RuntimeException("Game not found."));

        Optional<Match> existingMatch = matchRepository.findByUserAIdAndUserBIdAndGameIdAndStatus(sender.getId(), receiver.getId(), game.getId(), MatchStatus.PENDING);
        Optional<Match> reverseMatch = matchRepository.findByUserAIdAndUserBIdAndGameIdAndStatus(receiver.getId(), sender.getId(), game.getId(), MatchStatus.PENDING);

        if (existingMatch.isPresent() || reverseMatch.isPresent()) {
            throw new RuntimeException("A pending game request already exists with this user for this game.");
        }

        Match newMatch = new Match();
        newMatch.setUserAId(sender.getId());
        newMatch.setUserAUsername(sender.getUsername());
        newMatch.setUserBId(receiver.getId());
        newMatch.setUserBUsername(receiver.getUsername());
        newMatch.setGameId(game.getId());
        newMatch.setGameName(game.getName());
        newMatch.setStatus(MatchStatus.PENDING);
        newMatch.setMatchedAt(LocalDateTime.now());

        Match savedMatch = matchRepository.save(newMatch);

        Map<String, String> payload = Map.of(
                "type", "MATCH_REQUEST",
                "matchId", savedMatch.getId(),
                "senderUsername", sender.getUsername(),
                "gameName", game.getName()
        );

        messagingTemplate.convertAndSendToUser(receiver.getUsername(), "/queue/match-notifications", payload);
        return convertToMatchDetailsDTO(savedMatch);
    }

    public MatchDetailsDTO acceptGameMatch(String userId, String matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found."));

        if (!match.getStatus().equals(MatchStatus.PENDING) || !match.getUserBId().equals(userId)) {
            throw new RuntimeException("Invalid match status or user not authorized.");
        }

        match.setStatus(MatchStatus.ACCEPTED);
        match.setAcceptedAt(LocalDateTime.now());
        String chatRoomId = messageService.generateChatRoomId(match.getUserAId(), match.getUserBId());
        match.setChatRoomId(chatRoomId);
        Match updatedMatch = matchRepository.save(match);

        User sender = userRepository.findById(match.getUserAId()).orElseThrow();
        User receiver = userRepository.findById(match.getUserBId()).orElseThrow();

        Map<String, String> payloadA = Map.of(
                "type", "MATCH_ACCEPTED",
                "matchId", match.getId(),
                "gameName", match.getGameName(),
                "opponentUsername", receiver.getUsername()
        );

        Map<String, String> payloadB = Map.of(
                "type", "MATCH_ACCEPTED",
                "matchId", match.getId(),
                "gameName", match.getGameName(),
                "opponentUsername", sender.getUsername()
        );

        messagingTemplate.convertAndSendToUser(sender.getUsername(), "/queue/match-notifications", payloadA);
        messagingTemplate.convertAndSendToUser(receiver.getUsername(), "/queue/match-notifications", payloadB);

        return convertToMatchDetailsDTO(updatedMatch);
    }

    public MatchDetailsDTO declineGameMatch(String userId, String matchId) {
        Match match = matchRepository.findById(matchId).orElseThrow();

        if (!match.getStatus().equals(MatchStatus.PENDING) || !match.getUserBId().equals(userId)) {
            throw new RuntimeException("Invalid match status or unauthorized.");
        }

        match.setStatus(MatchStatus.DECLINED);
        match.setDeclinedAt(LocalDateTime.now());
        Match updatedMatch = matchRepository.save(match);

        User sender = userRepository.findById(match.getUserAId()).orElseThrow();
        User receiver = userRepository.findById(match.getUserBId()).orElseThrow();

        Map<String, String> payload = Map.of(
                "type", "MATCH_DECLINED",
                "matchId", match.getId(),
                "declinerUsername", receiver.getUsername(),
                "gameName", match.getGameName()
        );

        messagingTemplate.convertAndSendToUser(sender.getUsername(), "/queue/match-notifications", payload);
        messagingTemplate.convertAndSendToUser(receiver.getUsername(), "/queue/match-notifications", payload);

        return convertToMatchDetailsDTO(updatedMatch);
    }

    public MatchDetailsDTO closeGameMatch(String userId, String matchId) {
        Match match = matchRepository.findById(matchId).orElseThrow();

        if (!match.getStatus().equals(MatchStatus.ACCEPTED) ||
                (!match.getUserAId().equals(userId) && !match.getUserBId().equals(userId))) {
            throw new RuntimeException("Unauthorized or invalid match state.");
        }

        match.setStatus(MatchStatus.COMPLETED);
        match.setCompletedAt(LocalDateTime.now());
        Match closedMatch = matchRepository.save(match);

        User userA = userRepository.findById(match.getUserAId()).orElseThrow();
        User userB = userRepository.findById(match.getUserBId()).orElseThrow();

        userA.incrementMatchesPlayed();
        userB.incrementMatchesPlayed();
        userRepository.save(userA);
        badgeService.checkAndAssignBadges(userA);
        userRepository.save(userB);
        badgeService.checkAndAssignBadges(userB);

        String closerUsername = userRepository.findById(userId).get().getUsername();

        Map<String, String> payload = Map.of(
                "type", "MATCH_CLOSED",
                "matchId", closedMatch.getId(),
                "closerUsername", closerUsername
        );

        messagingTemplate.convertAndSendToUser(userA.getUsername(), "/queue/match-notifications", payload);
        messagingTemplate.convertAndSendToUser(userB.getUsername(), "/queue/match-notifications", payload);

        return convertToMatchDetailsDTO(closedMatch);
    }

    public MatchDetailsDTO submitMatchRating(String userId, String matchId, RatingRequestDTO ratingRequest) {
        Match match = matchRepository.findById(matchId).orElseThrow();

        if (!match.getStatus().equals(MatchStatus.COMPLETED) ||
                (!match.getUserAId().equals(userId) && !match.getUserBId().equals(userId)) ||
                (!match.getUserAId().equals(ratingRequest.getRatedUserId()) && !match.getUserBId().equals(ratingRequest.getRatedUserId())) ||
                userId.equals(ratingRequest.getRatedUserId())) {
            throw new RuntimeException("Invalid rating request.");
        }

        User ratedUser = userRepository.findById(ratingRequest.getRatedUserId()).orElseThrow();
        ratedUser.addRating(ratingRequest.getRating());
        userRepository.save(ratedUser);
        badgeService.checkAndAssignBadges(ratedUser);

        return convertToMatchDetailsDTO(match);
    }

    public List<MatchDetailsDTO> getPendingGameMatchesForUser(String userId) {
        return matchRepository.findByUserBIdAndStatus(userId, MatchStatus.PENDING)
                .stream()
                .map(this::convertToMatchDetailsDTO)
                .collect(Collectors.toList());
    }

    public List<MatchDetailsDTO> getAcceptedGameMatchesForUser(String userId) {
        return matchRepository.findByUserAIdAndStatusOrUserBIdAndStatus(
                        userId, MatchStatus.ACCEPTED, userId, MatchStatus.ACCEPTED)
                .stream()
                .map(this::convertToMatchDetailsDTO)
                .collect(Collectors.toList());
    }

    public MatchDetailsDTO getMatchDetails(String matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found with ID: " + matchId));
        return convertToMatchDetailsDTO(match);
    }

    private MatchDetailsDTO convertToMatchDetailsDTO(Match match) {
        return MatchDetailsDTO.builder()
                .id(match.getId())
                .userAId(match.getUserAId())
                .userAUsername(match.getUserAUsername())
                .userBId(match.getUserBId())
                .userBUsername(match.getUserBUsername())
                .gameId(match.getGameId())
                .gameName(match.getGameName())
                .status(match.getStatus().name())
                .matchedAt(match.getMatchedAt())
                .acceptedAt(match.getAcceptedAt())
                .declinedAt(match.getDeclinedAt())
                .completedAt(match.getCompletedAt())
                .chatRoomId(match.getChatRoomId())
                .build();
    }
}
