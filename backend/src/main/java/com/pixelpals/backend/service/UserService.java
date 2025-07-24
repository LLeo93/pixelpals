package com.pixelpals.backend.service;

import com.pixelpals.backend.enumeration.FriendshipStatus;
import com.pixelpals.backend.enumeration.SkillLevel;
import com.pixelpals.backend.model.*;
import com.pixelpals.backend.repository.FriendshipRepository;
import com.pixelpals.backend.repository.GameRepository;
import com.pixelpals.backend.repository.PlatformRepository;
import com.pixelpals.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final PlatformRepository platformRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final FriendshipRepository friendshipRepository;

    public UserService(UserRepository userRepository,
                       GameRepository gameRepository,
                       PlatformRepository platformRepository,
                       PasswordEncoder passwordEncoder,
                       CloudinaryService cloudinaryService,
                       FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.platformRepository = platformRepository;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryService = cloudinaryService;
        this.friendshipRepository = friendshipRepository;
    }

    public User updateAvatarUrl(String identifier, String avatarUrl) {
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setAvatarUrl(avatarUrl);
        return userRepository.save(user);
    }

    public User uploadAndSetAvatar(String identifier, MultipartFile file) throws IOException {
        String imageUrl = cloudinaryService.uploadFile(file);
        return updateAvatarUrl(identifier, imageUrl);
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato con email o username: " + identifier));
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void logAllAcceptedFriendships(String username) {
        Optional<User> userOpt = getUserByUsername(username);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        friendshipRepository.findBySenderOrReceiverAndStatus(user, user, FriendshipStatus.ACCEPTED);
    }

    public User saveUser(User user) {
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER");
        }
        return userRepository.save(user);
    }

    public User createUser(User user) {
        return saveUser(user);
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
        filteredUpdates.remove("avatarUrl");

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
                default -> {
                    // Ignorato
                }
            }
        });
        return userRepository.save(user);
    }

    public void setUserOnlineStatus(String userId, boolean status) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User with ID " + userId + " not found."));
        user.setOnline(status);
        userRepository.save(user);
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

    public List<User> getFriendsWithOnlineStatus(String userId) throws UsernameNotFoundException {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
        List<Friendship> friendships = friendshipRepository.findBySenderOrReceiverAndStatus(currentUser, currentUser, FriendshipStatus.ACCEPTED);
        return friendships.stream()
                .map(f -> f.getSender().getId().equals(userId) ? f.getReceiver() : f.getSender())
                .collect(Collectors.toList());
    }
}
