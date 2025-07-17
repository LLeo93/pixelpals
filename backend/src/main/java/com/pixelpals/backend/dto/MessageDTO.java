package com.pixelpals.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder; // Importa Builder
import lombok.Data;   // Importa Data (o Getter, Setter, NoArgsConstructor)
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; // Usa LocalDateTime per coerenza

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Aggiungi questa annotazione
public class MessageDTO {
    private String id;
    private String senderId;
    private String senderUsername;
    private String receiverId;
    private String receiverUsername;
    private String content;
    private LocalDateTime timestamp; // DEVE ESSERE LocalDateTime
    private boolean read;
    private String chatRoomId;
}
