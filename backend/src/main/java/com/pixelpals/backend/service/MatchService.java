package com.pixelpals.backend.service;

import com.pixelpals.backend.dto.GameMatchRequestDTO;
import com.pixelpals.backend.dto.MatchDetailsDTO;
import com.pixelpals.backend.dto.MatchRequestDTO;
import com.pixelpals.backend.dto.MatchedUserDTO;
import com.pixelpals.backend.model.Game;
import com.pixelpals.backend.model.Match; // Import the Match class
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.enumeration.MatchStatus; // Import the MatchStatus enum
import com.pixelpals.backend.repository.GameRepository;
import com.pixelpals.backend.repository.MatchRepository;
import com.pixelpals.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate; // Import SimpMessagingTemplate

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map; // Import Map
import java.util.HashMap; // Import HashMap

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final MessageService messageService; // Inject MessageService for the chat room
    private final SimpMessagingTemplate messagingTemplate; // Inject SimpMessagingTemplate

    // Existing method to find matches
    public List<MatchedUserDTO> findMatches(MatchRequestDTO request, String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found."));

        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(user -> !user.getId().equals(currentUser.getId())) // Exclude the current user
                .map(user -> {
                    // Compatibility logic (simplified for the example)
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
                        // Converts SkillLevel to String for comparison
                        String userSkill = user.getSkillLevelMap().get(request.getGameName()) != null ?
                                user.getSkillLevelMap().get(request.getGameName()).name() : null; // Correction: .name() for enum
                        if (userSkill != null && userSkill.equals(request.getSkillLevel())) {
                            score += 20;
                        }
                    }

                    // Retrieve common games and platforms for display
                    List<String> commonGames = currentUser.getPreferredGames().stream()
                            .filter(g -> user.getPreferredGames().contains(g))
                            .map(Game::getName)
                            .collect(Collectors.toList());

                    List<String> commonPlatforms = currentUser.getPlatforms().stream()
                            .filter(p -> user.getPlatforms().contains(p))
                            .map(platform -> platform.getName())
                            .collect(Collectors.toList());

                    // Retrieve the specific skill level for the searched game
                    String skillLevelForGame = null;
                    if (request.getGameName() != null && !request.getGameName().isEmpty()) {
                        skillLevelForGame = user.getSkillLevelMap().get(request.getGameName()) != null ?
                                user.getSkillLevelMap().get(request.getGameName()).name() : null; // Correction: .name() for enum
                    }

                    return MatchedUserDTO.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .level(user.getLevel())
                            .rating(user.getRating())
                            .avatarUrl(user.getAvatarUrl())
                            .isOnline(user.isOnline()) // Corrected: user.isOnline()
                            .compatibilityScore(score)
                            .commonGames(commonGames)
                            .commonPlatforms(commonPlatforms)
                            .skillLevelForGame(skillLevelForGame)
                            .build();
                })
                .filter(matchedUser -> matchedUser.getCompatibilityScore() > 0) // Moved filter after map
                .limit(request.getMaxResults())
                .collect(Collectors.toList());
    }

    // Existing method to request a game
    public MatchDetailsDTO requestGameMatch(String senderId, GameMatchRequestDTO requestDTO) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found."));
        User receiver = userRepository.findById(requestDTO.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found."));
        Game game = gameRepository.findById(requestDTO.getGameId())
                .orElseThrow(() -> new RuntimeException("Game not found."));

        // Check if a pending request already exists between these two users for this game
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

        return convertToMatchDetailsDTO(savedMatch);
    }

    // Existing method to accept a game
    public MatchDetailsDTO acceptGameMatch(String userId, String matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found."));

        if (!match.getStatus().equals(MatchStatus.PENDING)) {
            throw new RuntimeException("The match is not in pending status.");
        }

        // Verify that the accepting user is the receiver of the request
        if (!match.getUserBId().equals(userId)) {
            throw new RuntimeException("You are not authorized to accept this match.");
        }

        match.setStatus(MatchStatus.ACCEPTED);
        match.setAcceptedAt(LocalDateTime.now());
        Match updatedMatch = matchRepository.save(match);

        // Generate the chat room ID and save it in the match (if not already present)
        String chatRoomId = messageService.generateChatRoomId(updatedMatch.getUserAId(), updatedMatch.getUserBId());
        updatedMatch.setChatRoomId(chatRoomId); // Ensure your Match model has a chatRoomId field
        matchRepository.save(updatedMatch); // Save again to update chatRoomId

        System.out.println("DEBUG MatchService: Partita accettata. ID Match: " + updatedMatch.getId());
        System.out.println("DEBUG MatchService: ChatRoomId generato/assegnato: " + updatedMatch.getChatRoomId());

        // NUOVO: Invia notifica all'utente che ha inviato la richiesta (User A)
        // per reindirizzarlo alla MatchRoomPage
        User senderUser = userRepository.findById(updatedMatch.getUserAId())
                .orElseThrow(() -> new RuntimeException("Sender user not found for notification."));

        Map<String, String> notificationPayload = new HashMap<>();
        notificationPayload.put("type", "MATCH_ACCEPTED");
        notificationPayload.put("matchId", updatedMatch.getId());
        notificationPayload.put("gameName", updatedMatch.getGameName());
        notificationPayload.put("opponentUsername", updatedMatch.getUserBUsername()); // L'accettante è l'opponente per il mittente

        System.out.println("DEBUG MatchService: Invio notifica di accettazione a username: " + senderUser.getUsername());
        System.out.println("DEBUG MatchService: Payload notifica: " + notificationPayload);

        messagingTemplate.convertAndSendToUser(
                senderUser.getUsername(), // Invia al nome utente del mittente
                "/queue/unread-updates", // Coda privata per gli aggiornamenti del match (stessa coda dei messaggi non letti)
                notificationPayload
        );
        System.out.println("DEBUG MatchService: Notifica di accettazione partita inviata a " + senderUser.getUsername() + " per matchId: " + updatedMatch.getId());

        return convertToMatchDetailsDTO(updatedMatch);
    }

    // Existing method to decline a game
    public MatchDetailsDTO declineGameMatch(String userId, String matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found."));

        if (!match.getStatus().equals(MatchStatus.PENDING)) {
            throw new RuntimeException("The match is not in pending status.");
        }

        // Verify that the declining user is the receiver of the request
        if (!match.getUserBId().equals(userId)) {
            throw new RuntimeException("You are not authorized to decline this match.");
        }

        match.setStatus(MatchStatus.DECLINED);
        match.setDeclinedAt(LocalDateTime.now());
        // CORREZIONE: Assegna il risultato di save a updatedMatch
        Match updatedMatch = matchRepository.save(match);

        // Send decline notification to the original sender
        System.out.println("DEBUG MatchService: Partita rifiutata. Notifica il mittente.");

        return convertToMatchDetailsDTO(updatedMatch);
    }

    // Existing method to get pending requests
    public List<MatchDetailsDTO> getPendingGameMatchesForUser(String userId) {
        // Search for matches where the user is the receiver AND the status is PENDING
        List<Match> pendingMatches = matchRepository.findByUserBIdAndStatus(userId, MatchStatus.PENDING);
        // Alternatively, if you also want the sender to see their pending requests (awaiting response):
        // List<Match> sentPendingMatches = matchRepository.findByUserAIdAndStatus(userId, MatchStatus.PENDING);
        // pendingMatches.addAll(sentPendingMatches); // Combine lists, handling duplicates if necessary

        return pendingMatches.stream()
                .map(this::convertToMatchDetailsDTO)
                .collect(Collectors.toList());
    }

    // Existing method to get accepted games
    public List<MatchDetailsDTO> getAcceptedGameMatchesForUser(String userId) {
        // Search for matches where the user is userA or userB AND the status is ACCEPTED
        List<Match> acceptedMatches = matchRepository.findByUserAIdAndStatusOrUserBIdAndStatus(
                userId, MatchStatus.ACCEPTED, userId, MatchStatus.ACCEPTED
        );
        return acceptedMatches.stream()
                .map(this::convertToMatchDetailsDTO)
                .collect(Collectors.toList());
    }

    /**
     * NEW METHOD: Retrieves the details of a specific match.
     * Used by MatchRoomPage.
     * @param matchId The ID of the match.
     * @return DTO with match details.
     */
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
                .status(match.getStatus().name()) // Convert enum to String
                .matchedAt(match.getMatchedAt())
                .acceptedAt(match.getAcceptedAt())
                .declinedAt(match.getDeclinedAt())
                .completedAt(match.getCompletedAt())
                .chatRoomId(match.getChatRoomId()) // Include chatRoomId
                .build();
    }
}
