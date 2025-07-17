package com.pixelpals.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder; // Importa Builder
import lombok.Data;   // Importa Data
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Aggiungi questa annotazione
public class MatchDetailsDTO {
    private String id;
    private String userAId;
    private String userAUsername;
    private String userBId;
    private String userBUsername;
    private String gameId;
    private String gameName;
    private String status; // DEVE ESSERE String, non MatchStatus enum
    private LocalDateTime matchedAt;
    private LocalDateTime acceptedAt; // Campo aggiunto
    private LocalDateTime declinedAt; // Campo aggiunto
    private LocalDateTime completedAt; // Campo aggiunto
    private String chatRoomId; // Campo aggiunto
}