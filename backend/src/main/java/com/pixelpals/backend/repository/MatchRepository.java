package com.pixelpals.backend.repository;

import com.pixelpals.backend.model.Match;
// Rimosso: import com.pixelpals.backend.model.User; // Non più necessario qui se si query per ID String
import com.pixelpals.backend.enumeration.MatchStatus; // Importa l'enum MatchStatus
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository // È buona pratica aggiungere l'annotazione @Repository
public interface MatchRepository extends MongoRepository<Match, String> {

    // Metodo per trovare richieste di match pendenti in cui l'utente è UserB (il ricevitore)
    // Modificato per accettare String userId invece di User object
    List<Match> findByUserBIdAndStatus(String userBId, MatchStatus status);

    // Metodo per trovare match in cui l'utente è UserA e sono accettati
    // Modificato per accettare String userId invece di User object
    List<Match> findByUserAIdAndStatus(String userAId, MatchStatus status);

    // Metodo per controllare se esiste già una richiesta pendente tra due utenti per un dato gioco
    // Modificato per accettare String user IDs e String gameId
    Optional<Match> findByUserAIdAndUserBIdAndGameIdAndStatus(String userAId, String userBId, String gameId, MatchStatus status);

    // Potrebbe servire anche la controparte (ricevitore e mittente invertiti) per la richiesta pendente
    // Modificato per accettare String user IDs e String gameId
    Optional<Match> findByUserBIdAndUserAIdAndGameIdAndStatus(String userBId, String userAId, String gameId, MatchStatus status);

    // Metodo per ottenere tutte le partite accettate (attive) per l'utente autenticato (userA o userB)
    List<Match> findByUserAIdAndStatusOrUserBIdAndStatus(String userAId, MatchStatus statusA, String userBId, MatchStatus statusB);
}
