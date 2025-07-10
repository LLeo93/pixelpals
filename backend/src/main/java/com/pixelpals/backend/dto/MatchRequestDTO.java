package com.pixelpals.backend.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class MatchRequestDTO {
    private String gameName; // Nome del gioco per cui cercare un match
    private String platformName; // Nome della piattaforma preferita
    private String skillLevel; // Livello di skill desiderato (es. "INTERMEDIATE")
    private List<String> preferredTimeSlots; // Slot di disponibilità preferiti (es. "MONDAY_EVENING")
    private int maxResults; // Numero massimo di risultati da restituire
}
