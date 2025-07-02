package com.pixelpals.backend.service;
import com.pixelpals.backend.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final long refreshTokenDurationMs = 24 * 60 * 60 * 1000; // 1 giorno

    public RefreshTokenService(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    public String createRefreshToken(String username) {
        String token = UUID.randomUUID().toString();

        com.pixelpals.backend.entity.RefreshTokenEntity refreshToken = com.pixelpals.backend.entity.RefreshTokenEntity.builder()
                .token(token)
                .username(username)
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

        repository.save(refreshToken);
        return token;
    }

    public Optional<com.pixelpals.backend.entity.RefreshTokenEntity> findByToken(String token) {
        return repository.findByToken(token);
    }

    public void deleteRefreshToken(String username) {
        repository.deleteByUsername(username);
    }

    public boolean isExpired(com.pixelpals.backend.entity.RefreshTokenEntity token) {
        return token.getExpiryDate().isBefore(Instant.now());
    }
}
