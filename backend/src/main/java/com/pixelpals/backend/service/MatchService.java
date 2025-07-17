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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

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

        Optional<Match> existingPendingMatch = matchRepository.findByUserAIdAndUserBIdAndGameIdAndStatus(
                sender.getId(), receiver.getId(), game.getId(), MatchStatus.PENDING);
        if (existingPendingMatch.isPresent()) {
            throw new RuntimeException("A pending game request already exists for this game with this user.");
        }
        Optional<Match> existingPendingMatchReverse = matchRepository.findByUserAIdAndUserBIdAndGameIdAndStatus(
                receiver.getId(), sender.getId(), game.getId(), MatchStatus.PENDING);
        if (existingPendingMatchReverse.isPresent()) {
            throw new RuntimeException("You already have an incoming game request from this user for this game.");
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

        System.out.println("DEBUG MatchService: Richiesta partita creata. ID Match: " + savedMatch.getId());
        System.out.println("DEBUG MatchService: Mittente: " + sender.getUsername() + " (ID: " + sender.getId() + ")");
        System.out.println("DEBUG MatchService: Destinatario: " + receiver.getUsername() + " (ID: " + receiver.getId() + ")");

        // Invia notifica di richiesta partita al destinatario (User B)
        Map<String, String> notificationPayload = new HashMap<>();
        notificationPayload.put("type", "MATCH_REQUEST");
        notificationPayload.put("matchId", savedMatch.getId());
        notificationPayload.put("senderUsername", sender.getUsername());
        notificationPayload.put("gameName", game.getName());

        System.out.println("DEBUG MatchService: Invio notifica di richiesta a username: " + receiver.getUsername());
        System.out.println("DEBUG MatchService: Payload notifica richiesta: " + notificationPayload);

        messagingTemplate.convertAndSendToUser(
                receiver.getUsername(),
                "/queue/unread-updates",
                notificationPayload
        );
        System.out.println("DEBUG MatchService: Notifica di richiesta partita inviata a " + receiver.getUsername() + " per matchId: " + savedMatch.getId());

        return convertToMatchDetailsDTO(savedMatch);
    }

    public MatchDetailsDTO acceptGameMatch(String userId, String matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found."));

        if (!match.getStatus().equals(MatchStatus.PENDING)) {
            throw new RuntimeException("The match is not in pending status.");
        }

        if (!match.getUserBId().equals(userId)) {
            throw new RuntimeException("You are not authorized to accept this match.");
        }

        match.setStatus(MatchStatus.ACCEPTED);
        match.setAcceptedAt(LocalDateTime.now());
        Match updatedMatch = matchRepository.save(match);

        String chatRoomId = messageService.generateChatRoomId(updatedMatch.getUserAId(), updatedMatch.getUserBId());
        updatedMatch.setChatRoomId(chatRoomId);
        matchRepository.save(updatedMatch);

        System.out.println("DEBUG MatchService: Partita accettata. ID Match: " + updatedMatch.getId());
        System.out.println("DEBUG MatchService: ChatRoomId generato/assegnato: " + updatedMatch.getChatRoomId());

        User senderUser = userRepository.findById(updatedMatch.getUserAId())
                .orElseThrow(() -> new RuntimeException("Sender user not found for notification."));

        Map<String, String> notificationPayload = new HashMap<>();
        notificationPayload.put("type", "MATCH_ACCEPTED");
        notificationPayload.put("matchId", updatedMatch.getId());
        notificationPayload.put("gameName", updatedMatch.getGameName());
        notificationPayload.put("opponentUsername", updatedMatch.getUserBUsername());

        System.out.println("DEBUG MatchService: Invio notifica di accettazione a username: " + senderUser.getUsername());
        System.out.println("DEBUG MatchService: Payload notifica: " + notificationPayload);

        messagingTemplate.convertAndSendToUser(
                senderUser.getUsername(),
                "/queue/unread-updates",
                notificationPayload
        );
        System.out.println("DEBUG MatchService: Notifica di accettazione partita inviata a " + senderUser.getUsername() + " per matchId: " + updatedMatch.getId());

        return convertToMatchDetailsDTO(updatedMatch);
    }

    public MatchDetailsDTO declineGameMatch(String userId, String matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found."));

        if (!match.getStatus().equals(MatchStatus.PENDING)) {
            throw new RuntimeException("The match is not in pending status.");
        }

        if (!match.getUserBId().equals(userId)) {
            throw new RuntimeException("You are not authorized to decline this match.");
        }

        match.setStatus(MatchStatus.DECLINED);
        match.setDeclinedAt(LocalDateTime.now());
        Match updatedMatch = matchRepository.save(match);

        System.out.println("DEBUG MatchService: Partita rifiutata. Notifica il mittente.");

        User senderUser = userRepository.findById(updatedMatch.getUserAId())
                .orElseThrow(() -> new RuntimeException("Sender user not found for notification."));

        Map<String, String> notificationPayload = new HashMap<>();
        notificationPayload.put("type", "MATCH_DECLINED");
        notificationPayload.put("matchId", updatedMatch.getId());
        notificationPayload.put("declinerUsername", updatedMatch.getUserBUsername());
        notificationPayload.put("gameName", updatedMatch.getGameName());

        System.out.println("DEBUG MatchService: Invio notifica di rifiuto a username: " + senderUser.getUsername());
        System.out.println("DEBUG MatchService: Payload notifica rifiuto: " + notificationPayload);

        messagingTemplate.convertAndSendToUser(
                senderUser.getUsername(),
                "/queue/unread-updates",
                notificationPayload
        );
        System.out.println("DEBUG MatchService: Notifica di rifiuto partita inviata a " + senderUser.getUsername() + " per matchId: " + updatedMatch.getId());

        return convertToMatchDetailsDTO(updatedMatch);
    }

    public MatchDetailsDTO closeGameMatch(String userId, String matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found."));

        if (!match.getUserAId().equals(userId) && !match.getUserBId().equals(userId)) {
            throw new RuntimeException("You are not authorized to close this match.");
        }

        if (!match.getStatus().equals(MatchStatus.ACCEPTED)) {
            throw new RuntimeException("Only accepted matches can be closed.");
        }

        match.setStatus(MatchStatus.COMPLETED);
        match.setCompletedAt(LocalDateTime.now());
        Match closedMatch = matchRepository.save(match);

        System.out.println("DEBUG MatchService: Partita chiusa. ID Match: " + closedMatch.getId());
        System.out.println("DEBUG MatchService: Utente che ha chiuso: " + userId);

        // Aggiorna il contatore delle partite giocate e il livello per entrambi gli utenti
        User userA = userRepository.findById(closedMatch.getUserAId())
                .orElseThrow(() -> new RuntimeException("User A not found."));
        User userB = userRepository.findById(closedMatch.getUserBId())
                .orElseThrow(() -> new RuntimeException("User B not found."));

        userA.incrementMatchesPlayed(); // Incrementa le partite giocate e aggiorna il livello
        userB.incrementMatchesPlayed(); // Incrementa le partite giocate e aggiorna il livello

        userRepository.save(userA); // Salva l'utente A aggiornato
        userRepository.save(userB); // Salva l'utente B aggiornato

        System.out.println("DEBUG MatchService: Livello di " + userA.getUsername() + " aggiornato a: " + userA.getLevel());
        System.out.println("DEBUG MatchService: Livello di " + userB.getUsername() + " aggiornato a: " + userB.getLevel());


        String otherUserId = closedMatch.getUserAId().equals(userId) ? closedMatch.getUserBId() : closedMatch.getUserAId();
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("Other user not found for notification."));

        Map<String, String> notificationPayload = new HashMap<>();
        notificationPayload.put("type", "MATCH_CLOSED");
        notificationPayload.put("matchId", closedMatch.getId());
        notificationPayload.put("closerUsername", userRepository.findById(userId).get().getUsername());

        System.out.println("DEBUG MatchService: Invio notifica di chiusura a username: " + otherUser.getUsername());
        System.out.println("DEBUG MatchService: Payload notifica di chiusura: " + notificationPayload);

        messagingTemplate.convertAndSendToUser(
                otherUser.getUsername(),
                "/queue/unread-updates",
                notificationPayload
        );
        System.out.println("DEBUG MatchService: Notifica di chiusura partita inviata a " + otherUser.getUsername());

        return convertToMatchDetailsDTO(closedMatch);
    }

    public MatchDetailsDTO submitMatchRating(String userId, String matchId, RatingRequestDTO ratingRequest) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found."));

        if (!match.getStatus().equals(MatchStatus.COMPLETED)) {
            throw new RuntimeException("Only completed matches can be rated.");
        }
        if (!match.getUserAId().equals(userId) && !match.getUserBId().equals(userId)) {
            throw new RuntimeException("You are not authorized to rate this match.");
        }
        if (!match.getUserAId().equals(ratingRequest.getRatedUserId()) && !match.getUserBId().equals(ratingRequest.getRatedUserId())) {
            throw new RuntimeException("The user being rated is not part of this match.");
        }
        if (userId.equals(ratingRequest.getRatedUserId())) {
            throw new RuntimeException("You cannot rate yourself.");
        }

        User ratedUser = userRepository.findById(ratingRequest.getRatedUserId())
                .orElseThrow(() -> new RuntimeException("Rated user not found."));

        ratedUser.addRating(ratingRequest.getRating());
        userRepository.save(ratedUser);

        System.out.println("DEBUG MatchService: Rating sottomesso per utente: " + ratedUser.getUsername());
        System.out.println("DEBUG MatchService: Rating: " + ratingRequest.getRating());
        System.out.println("DEBUG MatchService: Feedback: " + ratingRequest.getFeedback());
        System.out.println("DEBUG MatchService: Nuovo rating medio di " + ratedUser.getUsername() + ": " + ratedUser.getRating());

        return convertToMatchDetailsDTO(match);
    }

    public List<MatchDetailsDTO> getPendingGameMatchesForUser(String userId) {
        List<Match> pendingMatches = matchRepository.findByUserBIdAndStatus(userId, MatchStatus.PENDING);
        return pendingMatches.stream()
                .map(this::convertToMatchDetailsDTO)
                .collect(Collectors.toList());
    }

    public List<MatchDetailsDTO> getAcceptedGameMatchesForUser(String userId) {
        List<Match> acceptedMatches = matchRepository.findByUserAIdAndStatusOrUserBIdAndStatus(
                userId, MatchStatus.ACCEPTED, userId, MatchStatus.ACCEPTED
        );
        return acceptedMatches.stream()
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
