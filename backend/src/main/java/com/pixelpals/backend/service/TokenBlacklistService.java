package com.pixelpals.backend.service;

import com.pixelpals.backend.model.BlacklistedToken;
import com.pixelpals.backend.repository.TokenBlacklistRepository;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private final TokenBlacklistRepository blacklistRepository;

    public TokenBlacklistService(TokenBlacklistRepository blacklistRepository) {
        this.blacklistRepository = blacklistRepository;
    }

    public void blacklistToken(String token) {
        // Salva il token nella collezione MongoDB
        blacklistRepository.save(new BlacklistedToken(token));
    }

    public boolean isTokenBlacklisted(String token) {
        // Controlla se il token è già presente nella blacklist
        return blacklistRepository.existsById(token);
    }
}
