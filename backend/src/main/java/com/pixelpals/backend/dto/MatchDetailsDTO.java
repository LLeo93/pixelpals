package com.pixelpals.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDetailsDTO {
    private String id;
    private String userAId;
    private String userAUsername;
    private String userBId;
    private String userBUsername;
    private String gameId;
    private String gameName;
    private String status;
    private LocalDateTime matchedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime declinedAt;
    private LocalDateTime completedAt;
    private String chatRoomId;
}