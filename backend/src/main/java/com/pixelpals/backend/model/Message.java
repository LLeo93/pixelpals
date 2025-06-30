package com.pixelpals.backend.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Message {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private User sender;

    @ManyToOne
    private User receiver;

    private String content;
    private LocalDateTime sentAt;

    @ManyToOne
    private Match match; // può essere null
}
