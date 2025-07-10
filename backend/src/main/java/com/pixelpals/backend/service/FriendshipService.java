package com.pixelpals.backend.service;

import com.pixelpals.backend.dto.FriendshipDTO;
import com.pixelpals.backend.dto.UserDTO; // Per restituire liste di amici
import com.pixelpals.backend.enumeration.FriendshipStatus;
import com.pixelpals.backend.mapper.FriendshipMapper;
import com.pixelpals.backend.mapper.UserMapper; // Per mappare User a UserDTO
import com.pixelpals.backend.model.Friendship;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.repository.FriendshipRepository;
import com.pixelpals.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importa Transactional

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository; // Necessario per recuperare gli oggetti User
    @Transactional
    public FriendshipDTO sendFriendRequest(String senderUsername, String receiverUsername) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Mittente non trovato: " + senderUsername));
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Destinatario non trovato: " + receiverUsername));
        if (sender.equals(receiver)) {
            throw new IllegalArgumentException("Non puoi inviare una richiesta di amicizia a te stesso.");
        }
        // Controlla se esiste già una richiesta PENDING o ACCEPTED in entrambe le direzioni
        Optional<Friendship> existingForward = friendshipRepository.findBySenderAndReceiver(sender, receiver);
        Optional<Friendship> existingBackward = friendshipRepository.findBySenderAndReceiver(receiver, sender);
        if (existingForward.isPresent()) {
            if (existingForward.get().getStatus() == FriendshipStatus.PENDING) {
                throw new IllegalArgumentException("Richiesta di amicizia già inviata e in sospeso.");
            } else if (existingForward.get().getStatus() == FriendshipStatus.ACCEPTED) {
                throw new IllegalArgumentException("Siete già amici.");
            }
        }
        if (existingBackward.isPresent()) {
            if (existingBackward.get().getStatus() == FriendshipStatus.PENDING) {
                throw new IllegalArgumentException("Hai già ricevuto una richiesta di amicizia da questo utente. Accettala invece di inviarne una nuova.");
            } else if (existingBackward.get().getStatus() == FriendshipStatus.ACCEPTED) {
                throw new IllegalArgumentException("Siete già amici.");
            }
        }
        Friendship friendship = new Friendship();
        friendship.setSender(sender);
        friendship.setReceiver(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendship.setCreatedAt(LocalDateTime.now());
        return FriendshipMapper.toDTO(friendshipRepository.save(friendship));
    }
    @Transactional
    public FriendshipDTO acceptFriendRequest(String requestId, String currentUsername) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Richiesta di amicizia non trovata."));
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Utente corrente non trovato."));

        if (!friendship.getReceiver().equals(currentUser)) {
            throw new IllegalArgumentException("Non sei autorizzato ad accettare questa richiesta.");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalArgumentException("La richiesta non è in stato PENDING.");
        }
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setAcceptedAt(LocalDateTime.now());
        return FriendshipMapper.toDTO(friendshipRepository.save(friendship));
    }
    @Transactional
    public FriendshipDTO rejectFriendRequest(String requestId, String currentUsername) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Richiesta di amicizia non trovata."));

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Utente corrente non trovato."));

        if (!friendship.getReceiver().equals(currentUser)) {
            throw new IllegalArgumentException("Non sei autorizzato a rifiutare questa richiesta.");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalArgumentException("La richiesta non è in stato PENDING.");
        }

        friendship.setStatus(FriendshipStatus.REJECTED);
        return FriendshipMapper.toDTO(friendshipRepository.save(friendship));
    }
    @Transactional
    public void removeFriend(String friendId, String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Utente corrente non trovato."));
        User friendToRemove = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Amico da rimuovere non trovato."));
        // Trova l'amicizia in entrambe le direzioni e con stato ACCEPTED
        Optional<Friendship> friendshipOpt = friendshipRepository
                .findBySenderAndReceiverAndStatus(currentUser, friendToRemove, FriendshipStatus.ACCEPTED).or(() -> friendshipRepository.findBySenderAndReceiverAndStatus(friendToRemove, currentUser, FriendshipStatus.ACCEPTED));

        if (friendshipOpt.isEmpty()) {
            throw new IllegalArgumentException("Non siete amici o l'amicizia non è stata trovata.");
        }
        friendshipRepository.delete(friendshipOpt.get());
    }
    public List<UserDTO> getFriends(String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato: " + username));
        List<Friendship> friendships = friendshipRepository.findBySenderOrReceiverAndStatus(currentUser, currentUser, FriendshipStatus.ACCEPTED);
        return friendships.stream()
                .map(friendship -> {
                    // Restituisce l'utente che non è l'utente corrente
                    User friendUser = friendship.getSender().equals(currentUser) ? friendship.getReceiver() : friendship.getSender();
                    return UserMapper.toDTO(friendUser);
                })
                .collect(Collectors.toList());
    }
    public List<FriendshipDTO> getPendingRequests(String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato: " + username));
        return friendshipRepository.findByReceiverAndStatus(currentUser, FriendshipStatus.PENDING)
                .stream()
                .map(FriendshipMapper::toDTO)
                .collect(Collectors.toList());
    }
    public List<FriendshipDTO> getSentRequests(String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato: " + username));
        return friendshipRepository.findBySenderAndStatus(currentUser, FriendshipStatus.PENDING)
                .stream()
                .map(FriendshipMapper::toDTO)
                .collect(Collectors.toList());
    }
}
