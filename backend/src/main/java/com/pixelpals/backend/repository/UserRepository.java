package com.pixelpals.backend.repository;
import com.pixelpals.backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationToken(String token);
    List<User> findByUsernameContainingIgnoreCase(String username);
}


