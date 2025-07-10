package com.pixelpals.backend.service;

import com.pixelpals.backend.model.Game;
import com.pixelpals.backend.model.Platform; // Importa Platform
import com.pixelpals.backend.model.TimeSlot;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.repository.GameRepository;
import com.pixelpals.backend.repository.PlatformRepository; // Importa PlatformRepository
import com.pixelpals.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.pixelpals.backend.enumeration.SkillLevel;
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
    private final PlatformRepository platformRepository; // <-- NUOVO: Inietta PlatformRepository
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, GameRepository gameRepository, PlatformRepository platformRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.platformRepository = platformRepository; // <-- NUOVO: Inizializza
        this.passwordEncoder = passwordEncoder;
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

        // Se la lista di nomi di giochi è vuota, potresti voler svuotare i giochi preferiti dell'utente
        // o non fare nulla. Qui, se la lista è vuota, non aggiorniamo i giochi preferiti.
        // Se vuoi permettere di svuotare, cambia la condizione.
        // if (games.isEmpty() && !gameNames.isEmpty()) { user.setPreferredGames(Collections.emptyList()); }
        if (games.isEmpty() && !gameNames.isEmpty()) return false; // Se non trovo giochi ma la lista non è vuota, errore.

        user.setPreferredGames(games);
        userRepository.save(user);
        return true;
    }

    // <-- NUOVO METODO: updatePlatforms
    public boolean updatePlatforms(String identifier, List<String> platformNames) {
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Platform> platforms = platformRepository.findAll().stream()
                .filter(p -> platformNames.contains(p.getName()))
                .collect(Collectors.toList());

        // Simile a updatePreferredGames, decidi il comportamento per lista vuota
        if (platforms.isEmpty() && !platformNames.isEmpty()) return false;

        user.setPlatforms(platforms);
        userRepository.save(user);
        return true;
    }
    // NUOVO METODO: updatePlatforms -->

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
        // Crea una copia modificabile della mappa degli aggiornamenti
        Map<String, Object> filteredUpdates = new HashMap<>(updates);

        // Rimuovi i campi che non devono essere modificati tramite questo metodo
        filteredUpdates.remove("id");
        filteredUpdates.remove("isOnline"); // Rimozione per il nome del campo nell'entità
        filteredUpdates.remove("online");   // Rimozione per il nome del campo nel DTO
        filteredUpdates.remove("preferredGames"); // Gestito da updatePreferredGames
        filteredUpdates.remove("platforms"); // Gestito da updatePlatforms
        filteredUpdates.remove("skillLevelMap"); // Gestito da updateSkillLevels
        filteredUpdates.remove("availability"); // Gestito da updateAvailability


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

                case "avatarUrl" -> user.setAvatarUrl((String) value);

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
                    // La password viene hashata qui se è stata fornita una nuova password
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
