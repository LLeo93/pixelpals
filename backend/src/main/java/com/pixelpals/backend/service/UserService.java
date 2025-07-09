package com.pixelpals.backend.service;

import com.pixelpals.backend.model.Game;
import com.pixelpals.backend.model.TimeSlot;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.repository.GameRepository;
import com.pixelpals.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.pixelpals.backend.enumeration.SkillLevel;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, GameRepository gameRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
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

    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Cripta la password
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER"); // Ruolo di default
        }
        return userRepository.save(user);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    // Aggiorna disponibilità (TimeSlots)
    public User updateAvailability(String identifier, List<TimeSlot> timeSlots) {
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setAvailability(timeSlots);
        return userRepository.save(user);
    }

    // Aggiorna giochi preferiti da lista nomi giochi
    public boolean updatePreferredGames(String identifier, List<String> gameNames) {
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Game> games = gameRepository.findAll().stream()
                .filter(g -> gameNames.contains(g.getName()))
                .collect(Collectors.toList());

        if (games.isEmpty()) return false;

        user.setPreferredGames(games);
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
            // SkillLevel.valueOf fallisce se il valore non corrisponde
            return false;
        }

    }
    public User updateUserFields(User user, Map<String, Object> updates) {
        updates.forEach((key, value) -> {
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
                    } else {
                        throw new IllegalArgumentException("Il campo 'level' deve essere un intero");
                    }
                }

                case "rating" -> {
                    try {
                        user.setRating(Double.parseDouble(value.toString()));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Il campo 'rating' deve essere un numero");
                    }
                }

                case "password" -> {
                    String rawPassword = (String) value;
                    user.setPassword(passwordEncoder.encode(rawPassword));
                }

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
    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        user.setVerified(false);

        // Genera token
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        User saved = userRepository.save(user);

        emailService.sendVerificationEmail(saved);
        return saved;
    }

    public boolean verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token non valido"));
        user.setVerified(true);
        userRepository.save(user);
        return true;
    }


}
