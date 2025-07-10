package com.pixelpals.backend.repository;

import com.pixelpals.backend.model.Friendship;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.enumeration.FriendshipStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface FriendshipRepository extends MongoRepository<Friendship, String> {
    // Trova una richiesta di amicizia tra due utenti specifici, indipendentemente dallo stato
    Optional<Friendship> findBySenderAndReceiver(User sender, User receiver);
    // Trova una richiesta di amicizia tra due utenti specifici con un dato stato
    Optional<Friendship> findBySenderAndReceiverAndStatus(User sender, User receiver, FriendshipStatus status);
    // Trova tutte le richieste in cui l'utente è il mittente con un dato stato
    List<Friendship> findBySenderAndStatus(User sender, FriendshipStatus status);
    // Trova tutte le richieste in cui l'utente è il destinatario con un dato stato
    List<Friendship> findByReceiverAndStatus(User receiver, FriendshipStatus status);
    // Trova tutte le amicizie ACCETTATE per un dato utente (sia come sender che come receiver)
    List<Friendship> findBySenderOrReceiverAndStatus(User sender, User receiver, FriendshipStatus status);
    // Trova una richiesta specifica tra due utenti, indipendentemente dall'ordine (utile per rimozione)
    Optional<Friendship> findBySenderAndReceiverOrReceiverAndSenderAndStatus(User sender1, User receiver1, User sender2, User receiver2, FriendshipStatus status);
}

