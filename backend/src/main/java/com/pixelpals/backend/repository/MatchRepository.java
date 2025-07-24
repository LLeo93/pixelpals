package com.pixelpals.backend.repository;
import com.pixelpals.backend.model.Match;
import com.pixelpals.backend.enumeration.MatchStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface MatchRepository extends MongoRepository<Match, String> {
    List<Match> findByUserBIdAndStatus(String userBId, MatchStatus status);
    List<Match> findByUserAIdAndStatus(String userAId, MatchStatus status);
    Optional<Match> findByUserAIdAndUserBIdAndGameIdAndStatus(String userAId, String userBId, String gameId, MatchStatus status);
    Optional<Match> findByUserBIdAndUserAIdAndGameIdAndStatus(String userBId, String userAId, String gameId, MatchStatus status);
    List<Match> findByUserAIdAndStatusOrUserBIdAndStatus(String userAId, MatchStatus statusA, String userBId, MatchStatus statusB);
}
