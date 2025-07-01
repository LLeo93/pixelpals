package com.pixelpals.backend.model;

import com.pixelpals.backend.enumeration.AuthProvider;
import com.pixelpals.backend.enumeration.SkillLevel;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.*;

@Document(collection = "users") // MongoDB collection
@Data
public class User {

    @Id
    private String id; // Mongo usa String (ObjectId) oppure UUID manuale

    private String username;
    private String email;
    private String passwordHash;
    private String avatarUrl;
    private String bio;
    private int level;
    private double rating;
    private boolean isOnline;

    private AuthProvider authProvider;

    // Lista di slot orari salvati come sottodocumenti
    private List<TimeSlot> availability = new ArrayList<>();

    // Riferimenti a piattaforme (DBRef = tipo foreign key Mongo)
    @DBRef
    private List<Platform> platforms = new ArrayList<>();

    @DBRef
    private List<Game> preferredGames = new ArrayList<>();

    // Mappa gioco → skillLevel, convertita in forma semplice
    private Map<String, SkillLevel> skillLevelMap = new HashMap<>();
}

