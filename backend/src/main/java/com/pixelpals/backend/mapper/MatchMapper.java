package com.pixelpals.backend.mapper;
import com.pixelpals.backend.dto.MatchDetailsDTO;
import com.pixelpals.backend.model.Match;
import org.springframework.stereotype.Component;
@Component
public class MatchMapper {
    public MatchDetailsDTO toDTO(Match match) {
        if (match == null) {
            return null;
        }
        return MatchDetailsDTO.builder()
                .id(match.getId())
                .userAId(match.getUserAId())
                .userAUsername(match.getUserAUsername())
                .userBId(match.getUserBId())
                .userBUsername(match.getUserBUsername())
                .gameId(match.getGameId())
                .gameName(match.getGameName())
                .matchedAt(match.getMatchedAt())
                .status(match.getStatus().name())
                .acceptedAt(match.getAcceptedAt())
                .declinedAt(match.getDeclinedAt())
                .completedAt(match.getCompletedAt())
                .chatRoomId(match.getChatRoomId())
                .build();
    }

}