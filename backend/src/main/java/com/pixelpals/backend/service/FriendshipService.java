package com.pixelpals.backend.service;

import com.pixelpals.backend.dto.FriendshipDTO;
import com.pixelpals.backend.dto.UserDTO;
import com.pixelpals.backend.mapper.FriendshipMapper;
import com.pixelpals.backend.mapper.UserMapper;
import com.pixelpals.backend.model.Friendship;
import com.pixelpals.backend.enumeration.FriendshipStatus;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.repository.FriendshipRepository;
import com.pixelpals.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public FriendshipDTO sendFriendRequest(String senderUsername, String receiverUsername) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Mittente non trovato: " + senderUsername));
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Destinatario non trovato: " + receiverUsername));

        if (sender.equals(receiver)) {
            throw new IllegalArgumentException("Non puoi inviare una richiesta di amicizia a te stesso.");
        }

        Optional<Friendship> existingFriendship = friendshipRepository
                .findBySenderAndReceiver(sender, receiver)
                .or(() -> friendshipRepository.findBySenderAndReceiver(receiver, sender));

        if (existingFriendship.isPresent()) {
            Friendship friendship = existingFriendship.get();
            if (friendship.getStatus() == FriendshipStatus.PENDING) {
                if (friendship.getSender().equals(sender)) {
                    throw new IllegalArgumentException("Richiesta di amicizia già inviata e in sospeso a " + receiverUsername + ".");
                } else {
                    throw new IllegalArgumentException("Hai già ricevuto una richiesta di amicizia da " + receiverUsername + ". Accettala invece di inviarne una nuova.");
                }
            } else if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new IllegalArgumentException("Siete già amici con " + receiverUsername + ".");
            } else if (friendship.getStatus() == FriendshipStatus.REJECTED) {
                friendship.setStatus(FriendshipStatus.PENDING);
                friendship.setCreatedAt(LocalDateTime.now());
                friendship.setAcceptedAt(null);
                Friendship savedFriendship = friendshipRepository.save(friendship);
                messagingTemplate.convertAndSendToUser(
                        receiver.getUsername(),
                        "/queue/friend-requests/new",
                        FriendshipMapper.toDTO(savedFriendship)
                );
                return FriendshipMapper.toDTO(savedFriendship);
            }
        }

        Friendship friendship = new Friendship();
        friendship.setSender(sender);
        friendship.setReceiver(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendship.setCreatedAt(LocalDateTime.now());
        Friendship savedFriendship = friendshipRepository.save(friendship);
        messagingTemplate.convertAndSendToUser(
                receiver.getUsername(),
                "/queue/friend-requests/new",
                FriendshipMapper.toDTO(savedFriendship)
        );
        return FriendshipMapper.toDTO(savedFriendship);
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
        Friendship updatedFriendship = friendshipRepository.save(friendship);

        messagingTemplate.convertAndSendToUser(
                friendship.getSender().getUsername(),
                "/queue/friend-requests/update",
                FriendshipMapper.toDTO(updatedFriendship)
        );
        messagingTemplate.convertAndSendToUser(
                friendship.getReceiver().getUsername(),
                "/queue/friend-requests/update",
                FriendshipMapper.toDTO(updatedFriendship)
        );

        return FriendshipMapper.toDTO(updatedFriendship);
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
        Friendship updatedFriendship = friendshipRepository.save(friendship);

        messagingTemplate.convertAndSendToUser(
                friendship.getSender().getUsername(),
                "/queue/friend-requests/update",
                FriendshipMapper.toDTO(updatedFriendship)
        );
        messagingTemplate.convertAndSendToUser(
                friendship.getReceiver().getUsername(),
                "/queue/friend-requests/update",
                FriendshipMapper.toDTO(updatedFriendship)
        );

        return FriendshipMapper.toDTO(updatedFriendship);
    }

    @Transactional
    public void removeFriend(String friendId, String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Utente corrente non trovato."));
        User friendToRemove = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Amico da rimuovere non trovato."));

        Optional<Friendship> friendshipOpt = friendshipRepository
                .findBySenderAndReceiverAndStatus(currentUser, friendToRemove, FriendshipStatus.ACCEPTED)
                .or(() -> friendshipRepository.findBySenderAndReceiverAndStatus(friendToRemove, currentUser, FriendshipStatus.ACCEPTED));

        if (friendshipOpt.isEmpty()) {
            throw new IllegalArgumentException("Non siete amici o l'amicizia non è stata trovata.");
        }

        friendshipRepository.delete(friendshipOpt.get());

        messagingTemplate.convertAndSendToUser(
                currentUser.getUsername(),
                "/queue/friend-requests/update",
                Map.of("type", "FRIENDSHIP_REMOVED", "friendId", friendToRemove.getId())
        );
        messagingTemplate.convertAndSendToUser(
                friendToRemove.getUsername(),
                "/queue/friend-requests/update",
                Map.of("type", "FRIENDSHIP_REMOVED", "friendId", currentUser.getId())
        );
    }

    public List<UserDTO> getFriends(String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato: " + username));
        List<Friendship> friendships = getAcceptedFriendships(currentUser);
        Set<UserDTO> uniqueFriends = friendships.stream()
                .map(friendship -> {
                    User friendUser = friendship.getSender().equals(currentUser) ? friendship.getReceiver() : friendship.getSender();
                    return UserMapper.toDTO(friendUser);
                })
                .collect(Collectors.toSet());
        return new ArrayList<>(uniqueFriends);
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

    public List<Friendship> getAcceptedFriendships(User user) {
        List<Friendship> sentAccepted = friendshipRepository.findBySenderAndStatus(user, FriendshipStatus.ACCEPTED);
        List<Friendship> receivedAccepted = friendshipRepository.findByReceiverAndStatus(user, FriendshipStatus.ACCEPTED);
        Set<Friendship> allAccepted = new HashSet<>(sentAccepted);
        allAccepted.addAll(receivedAccepted);
        return new ArrayList<>(allAccepted);
    }

    public FriendshipStatus getFriendshipStatusBetweenUsers(String currentUsername, String otherUserId) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Utente corrente non trovato: " + currentUsername));
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("Utente cercato non trovato con ID: " + otherUserId));

        if (currentUser.getId().equals(otherUser.getId())) {
            return FriendshipStatus.SELF;
        }

        Optional<Friendship> friendshipOpt = friendshipRepository
                .findBySenderAndReceiver(currentUser, otherUser)
                .or(() -> friendshipRepository.findBySenderAndReceiver(otherUser, currentUser));

        if (friendshipOpt.isPresent()) {
            Friendship friendship = friendshipOpt.get();
            if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
                return FriendshipStatus.ACCEPTED;
            } else if (friendship.getStatus() == FriendshipStatus.PENDING) {
                if (friendship.getSender().getId().equals(currentUser.getId())) {
                    return FriendshipStatus.PENDING_SENT;
                } else {
                    return FriendshipStatus.PENDING_RECEIVED;
                }
            } else if (friendship.getStatus() == FriendshipStatus.REJECTED) {
                return FriendshipStatus.REJECTED;
            }
        }

        return FriendshipStatus.NONE;
    }
}
