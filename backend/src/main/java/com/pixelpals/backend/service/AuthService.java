package com.pixelpals.backend.service;

import com.pixelpals.backend.dto.AuthRequest;
import com.pixelpals.backend.dto.AuthResponse;
import com.pixelpals.backend.dto.RegisterRequest;
import com.pixelpals.backend.model.RefreshToken;
import com.pixelpals.backend.model.User;
import lombok.Data;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
@Data
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;  // nuova service per refresh token
    private final TokenBlacklistService blacklistService;    // per logout blacklist

    public AuthService(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager,
                       RefreshTokenService refreshTokenService, TokenBlacklistService blacklistService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.blacklistService = blacklistService;
    }

    public void register(RegisterRequest request) {
        Optional<User> existingUser = userService.getUserByUsername(request.getUsername());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Username già utilizzato");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setRole("ROLE_USER");
        userService.saveUser(user);
    }

    // login restituisce sia access token JWT che refresh token
    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // Logout: blacklistiamo access token e eliminiamo refresh token
    public void logout(String token, String username) {
        blacklistService.blacklistToken(token);
        refreshTokenService.deleteRefreshToken(username);
    }

    // Rigenera access token usando refresh token valido
    public AuthResponse refreshToken(String refreshToken) {
        Optional<com.pixelpals.backend.entity.RefreshTokenEntity> storedTokenOpt = refreshTokenService.findByToken(refreshToken);

        if (storedTokenOpt.isEmpty()) {
            throw new RuntimeException("Refresh token non valido");
        }

        com.pixelpals.backend.entity.RefreshTokenEntity storedToken = storedTokenOpt.get();

        if (refreshTokenService.isExpired(storedToken)) {
            refreshTokenService.deleteRefreshToken(storedToken.getUsername());
            throw new RuntimeException("Refresh token scaduto");
        }

        String newAccessToken = jwtService.generateToken(userService.loadUserByUsername(storedToken.getUsername()));
        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
