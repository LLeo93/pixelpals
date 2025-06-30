package com.pixelpals.backend.model;
import com.pixelpals.backend.enumeration.MatchStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Match {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private User userA;

    @ManyToOne
    private User userB;

    @ManyToOne
    private Game game;

    private LocalDateTime matchedAt;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;
}