package com.pixelpals.backend.model;

import com.pixelpals.backend.enumeration.MatchStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;

@Data
@Document(collection = "matches")
public class Match {
    @Id
    private String id;
    @DBRef
    private User userA;
    @DBRef
    private User userB;
    @DBRef
    private Game game;
    private LocalDateTime matchedAt;
    private MatchStatus status;
}
