package com.pixelpals.backend.model;
import com.pixelpals.backend.enumeration.AuthProvider;
import com.pixelpals.backend.enumeration.SkillLevel;
import jakarta.persistence.*;
import java.util.*;

@Entity
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    private String username;
    private String email;
    private String passwordHash;
    private String avatarUrl;
    private String bio;
    private int level;
    private double rating;
    private boolean isOnline;

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<TimeSlot> availability;

    @ManyToMany
    @JoinTable(
            name = "user_platform",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "platform_id")
    )
    private List<Platform> platforms;

    @ManyToMany
    @JoinTable(
            name = "user_game",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "game_id")
    )
    private List<Game> preferredGames;

    @ElementCollection
    @CollectionTable(name = "user_skill_level", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyJoinColumn(name = "game_id")
    @Column(name = "skill_level")
    @Enumerated(EnumType.STRING)
    private Map<Game, SkillLevel> skillLevelMap;
}

