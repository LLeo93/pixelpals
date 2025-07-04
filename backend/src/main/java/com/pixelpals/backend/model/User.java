package com.pixelpals.backend.model;

import com.pixelpals.backend.enumeration.AuthProvider;
import com.pixelpals.backend.enumeration.SkillLevel;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Document(collection = "users") // MongoDB collection
@Data
public class User implements UserDetails {

    @Id
    private String id;

    private String username;
    private String password;
    private String email;
    private String role;
    //private String passwordHash;
    private String avatarUrl;
    private String bio;
    private int level;
    private double rating;
    private boolean isOnline;

    private AuthProvider authProvider;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


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

