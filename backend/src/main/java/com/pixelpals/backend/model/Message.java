package com.pixelpals.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime; // Importa LocalDateTime

@Data // Fornisce getter, setter, equals, hashCode, toString
@Document(collection = "messages")
public class Message {
    @Id
    private String id;
    @DBRef
    private User sender;
    @DBRef
    private User receiver;
    private String content;
    private LocalDateTime timestamp; // DEVE ESSERE LocalDateTime
    private boolean read;
    private String chatRoomId; // Campo aggiunto per le chat di partita
}
