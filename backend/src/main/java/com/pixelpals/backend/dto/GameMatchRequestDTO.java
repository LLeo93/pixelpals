package com.pixelpals.backend.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameMatchRequestDTO {
    private String receiverId; // L'ID dell'utente a cui si invia la richiesta di partita
    private String gameId;     // L'ID del gioco per cui si desidera giocare
}
