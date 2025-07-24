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
    Optional<Friendship> findBySenderAndReceiver(User sender, User receiver);
    Optional<Friendship> findBySenderAndReceiverAndStatus(User sender, User receiver, FriendshipStatus status);
    List<Friendship> findBySenderAndStatus(User sender, FriendshipStatus status);
    List<Friendship> findByReceiverAndStatus(User receiver, FriendshipStatus status);
    List<Friendship> findBySenderOrReceiverAndStatus(User sender, User receiver, FriendshipStatus status);
}
