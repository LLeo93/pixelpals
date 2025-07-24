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
@Document(collection = "users")
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
    private double rating;
    private boolean isOnline;
    private boolean verified = false;
    private String verificationToken;
    private Date tokenExpirationDate;
    private double totalRatingPoints;
    private int numberOfRatings;
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
        return this.verified;
    }
    @Override
    public String getUsername() { return username; }
    private List<TimeSlot> availability;
    @DBRef
    private List<Platform> platforms = new ArrayList<>();
    @DBRef
    private List<Game> preferredGames = new ArrayList<>();
    private Map<String, SkillLevel> skillLevelMap = new HashMap<>();
    @DBRef
    private List<Badge> badges = new ArrayList<>();
    public void addRating(int newRating) {
        this.totalRatingPoints += newRating;
        this.numberOfRatings++;
        if (this.numberOfRatings > 0) {
            this.rating = this.totalRatingPoints / this.numberOfRatings;
        } else {
            this.rating = 0.0;
        }
    }
    public void incrementMatchesPlayed() {
        this.matchesPlayed++;
        updateLevel();
    }
    private void updateLevel() {
        if (this.matchesPlayed > 0) {
            this.level = (int) Math.floor(this.matchesPlayed / 5.0) + 1;
        } else {
            this.level = 0;
        }
    }
}
