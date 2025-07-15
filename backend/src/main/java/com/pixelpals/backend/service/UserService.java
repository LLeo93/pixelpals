package com.pixelpals.backend.service;

import com.pixelpals.backend.model.Game;
import com.pixelpals.backend.model.Platform;
import com.pixelpals.backend.model.TimeSlot;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.repository.GameRepository;
import com.pixelpals.backend.repository.PlatformRepository;
import com.pixelpals.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.pixelpals.backend.enumeration.SkillLevel;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.HashMap;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final PlatformRepository platformRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService; // Dichiarazione

    // COSTRUTTORE AGGIORNATO: INIETTARE CloudinaryService
    public UserService(UserRepository userRepository, GameRepository gameRepository,
                       PlatformRepository platformRepository, PasswordEncoder passwordEncoder,
                       CloudinaryService cloudinaryService) { // Aggiunto CloudinaryService qui
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.platformRepository = platformRepository;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryService = cloudinaryService; // INIZIALIZZAZIONE!
    }

    // NUOVO METODO: Aggiorna l'avatar dell'utente con la URL fornita
    public User updateAvatarUrl(String identifier, String avatarUrl) {
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setAvatarUrl(avatarUrl);
        return userRepository.save(user);
    }
    // NUOVO METODO: Gestisce l'upload del file avatar e aggiorna l'URL
    public User uploadAndSetAvatar(String identifier, MultipartFile file) throws IOException {
        String imageUrl = cloudinaryService.uploadFile(file); // Carica il file
        return updateAvatarUrl(identifier, imageUrl); // Aggiorna l'utente con la nuova URL
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        System.out.println("Caricamento utente con email o username: " + identifier);

        Optional<User> userOpt = userRepository.findByEmail(identifier);

        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByUsername(identifier);
        }

        User user = userOpt.orElseThrow(() ->
                new UsernameNotFoundException("Utente non trovato con email o username: " + identifier));

        System.out.println("Utente trovato: " + user.getEmail());
        return user;
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER");
        }
        return userRepository.save(user);
    }

    public User createUser(User user) {
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER");
        }
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> searchUsersByUsername(String usernameQuery) {
        if (usernameQuery == null || usernameQuery.trim().isEmpty()) {
            return userRepository.findAll();
        }
        return userRepository.findByUsernameContainingIgnoreCase(usernameQuery);
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public User updateAvailability(String identifier, List<TimeSlot> timeSlots) {
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setAvailability(timeSlots);
        return userRepository.save(user);
    }

    public boolean updatePreferredGames(String identifier, List<String> gameNames) {
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Game> games = gameRepository.findAll().stream()
                .filter(g -> gameNames.contains(g.getName()))
                .collect(Collectors.toList());

        if (games.isEmpty() && !gameNames.isEmpty()) return false;

        user.setPreferredGames(games);
        userRepository.save(user);
        return true;
    }

    public boolean updatePlatforms(String identifier, List<String> platformNames) {
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Platform> platforms = platformRepository.findAll().stream()
                .filter(p -> platformNames.contains(p.getName()))
                .collect(Collectors.toList());

        if (platforms.isEmpty() && !platformNames.isEmpty()) return false;

        user.setPlatforms(platforms);
        userRepository.save(user);
        return true;
    }

    public boolean updateSkillLevels(String identifier, Map<String, String> skillLevels) {
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        try {
            Map<String, SkillLevel> skillMap = skillLevels.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> SkillLevel.valueOf(entry.getValue().toUpperCase())
                    ));
            user.setSkillLevelMap(skillMap);
            userRepository.save(user);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public User updateUserFields(User user, Map<String, Object> updates) {
        Map<String, Object> filteredUpdates = new HashMap<>(updates);

        filteredUpdates.remove("id");
        filteredUpdates.remove("isOnline");
        filteredUpdates.remove("online");
        filteredUpdates.remove("preferredGames");
        filteredUpdates.remove("platforms");
        filteredUpdates.remove("skillLevelMap");
        filteredUpdates.remove("availability");
        filteredUpdates.remove("avatarUrl"); // Rimosso anche qui per coerenza con il nuovo endpoint dedicato

        filteredUpdates.forEach((key, value) -> {
            switch (key) {
                case "username" -> {
                    String newUsername = (String) value;
                    if (!newUsername.equals(user.getUsername()) &&
                            userRepository.existsByUsername(newUsername)) {
                        throw new IllegalArgumentException("Username già esistente");
                    }
                    user.setUsername(newUsername);
                }

                case "email" -> {
                    String newEmail = (String) value;
                    if (!newEmail.equals(user.getEmail()) &&
                            userRepository.existsByEmail(newEmail)) {
                        throw new IllegalArgumentException("Email già registrata");
                    }
                    user.setEmail(newEmail);
                }

                // Il caso "avatarUrl" è stato rimosso da qui. Sarà gestito dai metodi specifici:
                // updateAvatarUrl (per URL dirette) e uploadAndSetAvatar (per upload di file).
                case "bio" -> user.setBio((String) value);

                case "level" -> {
                    if (value instanceof Integer) {
                        user.setLevel((Integer) value);
                    } else if (value instanceof Number) {
                        user.setLevel(((Number) value).intValue());
                    } else {
                        throw new IllegalArgumentException("Il campo 'level' deve essere un intero");
                    }
                }

                case "rating" -> {
                    if (value instanceof Number) {
                        user.setRating(((Number) value).doubleValue());
                    } else if (value instanceof String) {
                        try {
                            user.setRating(Double.parseDouble((String) value));
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Il campo 'rating' deve essere un numero");
                        }
                    } else {
                        throw new IllegalArgumentException("Il campo 'rating' deve essere un numero");
                    }
                }

                case "password" -> {
                    String rawPassword = (String) value;
                    if (rawPassword != null && !rawPassword.isEmpty()) {
                        user.setPassword(passwordEncoder.encode(rawPassword));
                    }
                }

                case "role" -> user.setRole((String) value);
                case "verified" -> user.setVerified((Boolean) value);

                default -> throw new IllegalArgumentException("Campo '" + key + "' non supportato per l'aggiornamento");
            }
        });

        return userRepository.save(user);
    }

    public void setUserOnlineStatus(String userId, boolean status) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setOnline(status);
            userRepository.save(user);
        });
    }

    public void verifyUserEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token di verifica non valido o già utilizzato."));

        if (user.isVerified()) {
            throw new RuntimeException("L'account è già stato verificato.");
        }

        if (user.getTokenExpirationDate() == null || user.getTokenExpirationDate().before(new Date())) {
            throw new RuntimeException("Il token di verifica è scaduto.");
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpirationDate(null);
        userRepository.save(user);
    }
}