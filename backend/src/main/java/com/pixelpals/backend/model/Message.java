package com.pixelpals.backend.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import java.util.Date;
@Data
@Document(collection = "messages")
public class Message {

    @Id
    private String id;

    @DBRef // Riferimento all'utente mittente
    private User sender;

    @DBRef // Riferimento all'utente destinatario (per chat private)
    private User receiver;

    private String content; // Contenuto del messaggio
    private Date timestamp; // Data e ora del messaggio
    private String chatRoomId; // ID della chat room
}

