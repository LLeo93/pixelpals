package com.pixelpals.backend.service;

import com.pixelpals.backend.dto.UserMatchDTO;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.model.TimeSlot;
import com.pixelpals.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MatchService {

    private final UserRepository userRepository;

    public MatchService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserMatchDTO> findMatches(String username) {
        User currentUser = getUserOrThrow(username);

        return userRepository.findAll().stream()
                .filter(u -> !u.getUsername().equals(username))
                .filter(u -> u.getPreferredGames() != null && currentUser.getPreferredGames() != null)
                .filter(u -> !u.getPreferredGames().isEmpty())
                .filter(u -> u.getPreferredGames().stream().anyMatch(currentUser.getPreferredGames()::contains))
                .map(UserMatchDTO::fromUser)
                .collect(Collectors.toList());
    }

    public List<UserMatchDTO> findMatchesByGame(String username, String game) {
        getUserOrThrow(username); // solo per validazione

        return userRepository.findAll().stream()
                .filter(u -> !u.getUsername().equals(username))
                .filter(u -> u.getPreferredGames() != null && u.getPreferredGames().contains(game))
                .map(UserMatchDTO::fromUser)
                .collect(Collectors.toList());
    }

    public List<UserMatchDTO> findMatchesBySkillLevel(String username) {
        User currentUser = getUserOrThrow(username);
        int level = currentUser.getLevel();

        return userRepository.findAll().stream()
                .filter(u -> !u.getUsername().equals(username))
                .filter(u -> u.getLevel() == level)
                .map(UserMatchDTO::fromUser)
                .collect(Collectors.toList());
    }

    public List<UserMatchDTO> findCombinedMatches(String username) {
        User currentUser = getUserOrThrow(username);

        return userRepository.findAll().stream()
                .filter(u -> !u.getUsername().equals(username))
                .filter(u -> matchByGame(currentUser, u))
                .filter(u -> matchByAvailability(currentUser, u))
                .filter(u -> matchByLevel(currentUser, u))
                .map(UserMatchDTO::fromUser)
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + username));
    }

    private boolean matchByGame(User current, User other) {
        return current.getPreferredGames() != null &&
                other.getPreferredGames() != null &&
                other.getPreferredGames().stream().anyMatch(current.getPreferredGames()::contains);
    }

    private boolean matchByAvailability(User current, User other) {
        List<TimeSlot> currentSlots = current.getAvailability();
        List<TimeSlot> otherSlots = other.getAvailability();

        if (currentSlots == null || otherSlots == null) return false;

        return otherSlots.stream().anyMatch(otherSlot ->
                currentSlots.stream().anyMatch(currentSlot ->
                        Objects.equals(otherSlot.getDayOfWeek(), currentSlot.getDayOfWeek()) &&
                                Objects.equals(otherSlot.getStartTime(), currentSlot.getStartTime()) &&
                                Objects.equals(otherSlot.getEndTime(), currentSlot.getEndTime())
                )
        );
    }

    private boolean matchByLevel(User current, User other) {
        return other.getLevel() == current.getLevel();
    }
}
