package com.pixelpals.backend.model;
import com.pixelpals.backend.enumeration.FriendshipStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "friendships")
@Data
public class Friendship {
    @Id
    private String id;
    @DBRef // Riferimento all'utente che ha inviato la richiesta
    private User sender;
    @DBRef // Riferimento all'utente che ha ricevuto la richiesta
    private User receiver;
    private FriendshipStatus status; // Stato della richiesta: PENDING, ACCEPTED, REJECTED
    private LocalDateTime createdAt; // Data e ora di creazione della richiesta
    private LocalDateTime acceptedAt; // Data e ora di accettazione (null se non accettata)
}

