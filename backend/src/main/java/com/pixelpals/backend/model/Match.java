package com.pixelpals.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.pixelpals.backend.enumeration.MatchStatus; // Importa l'enum MatchStatus esistente

import java.time.LocalDateTime; // Importa LocalDateTime

@Data // Fornisce getter, setter, equals, hashCode, toString
@Document(collection = "matches")
public class Match {
    @Id
    private String id;

    // Memorizziamo gli ID e i nomi degli utenti direttamente per semplicità e performance nel contesto del match
    private String userAId;
    private String userAUsername;
    private String userBId;
    private String userBUsername;

    // Memorizziamo l'ID e il nome del gioco direttamente
    private String gameId;
    private String gameName;

    private MatchStatus status; // Usa l'enum importato
    private LocalDateTime matchedAt;
    private LocalDateTime acceptedAt; // Campo aggiunto
    private LocalDateTime declinedAt; // Campo aggiunto
    private LocalDateTime completedAt; // Campo aggiunto
    private String chatRoomId; // Campo aggiunto per la chat room

    // Rimosso: la definizione interna dell'enum MatchStatus, ora importato
}
