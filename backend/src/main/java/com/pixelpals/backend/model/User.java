package com.pixelpals.backend.model;
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
    private String avatarUrl;
    private String bio;
    private int level;
    private double rating; // Rating medio
    private boolean isOnline;
    private boolean verified = false; // Questo campo controlla l'abilitazione
    private String verificationToken;
    private Date tokenExpirationDate;
    private double totalRatingPoints; // Somma di tutti i rating ricevuti
    private int numberOfRatings;      // Numero di rating ricevuti
    private int matchesPlayed;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String effectiveRole = (role == null || role.trim().isEmpty()) ? "ROLE_USER" : role;
        return Collections.singletonList(new SimpleGrantedAuthority(effectiveRole));
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
        // Un utente è abilitato solo se è verificato
        return this.verified; // <--- QUESTA RIGA CAUSA LA DISABILITAZIONE SE 'verified' è false
    }
    @Override
    public String getUsername() { return username; }
    private List<TimeSlot> availability;
    // Riferimenti a piattaforme (DBRef = tipo foreign key Mongo)
    @DBRef
    private List<Platform> platforms = new ArrayList<>();
    @DBRef
    private List<Game> preferredGames = new ArrayList<>();
    // Mappa gioco → skillLevel, convertita in forma semplice
    private Map<String, SkillLevel> skillLevelMap = new HashMap<>();
    @DBRef
    private List<Badge> badges = new ArrayList<>();

    // NUOVO METODO: Per aggiungere un rating e aggiornare il rating medio dell'utente
    public void addRating(int newRating) {
        this.totalRatingPoints += newRating;
        this.numberOfRatings++;
        // Calcola il nuovo rating medio
        if (this.numberOfRatings > 0) {
            this.rating = this.totalRatingPoints / this.numberOfRatings;
        } else {
            this.rating = 0.0; // Nessun rating, imposta a 0
        }
    }
    // NUOVO METODO: Per incrementare il contatore delle partite giocate e aggiornare il livello
    public void incrementMatchesPlayed() {
        this.matchesPlayed++;
        updateLevel(); // Aggiorna il livello dopo aver incrementato le partite giocate
    }

    // NUOVO METODO: Logica per aggiornare il livello in base alle partite giocate
    private void updateLevel() {
        // Esempio di logica di livellamento: 1 livello ogni 5 partite
        // Puoi personalizzare questa formula come preferisci
        if (this.matchesPlayed > 0) {
            this.level = (int) Math.floor(this.matchesPlayed / 5.0) + 1;
        } else {
            this.level = 0; // Livello iniziale
        }
    }
}
