package com.pixelpals.backend.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.pixelpals.backend.enumeration.MatchStatus;
import java.time.LocalDateTime;
@Data
@Document(collection = "matches")
public class Match {
    @Id
    private String id;
    private String userAId;
    private String userAUsername;
    private String userBId;
    private String userBUsername;
    private String gameId;
    private String gameName;
    private MatchStatus status;
    private LocalDateTime matchedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime declinedAt;
    private LocalDateTime completedAt;
    private String chatRoomId;
}
