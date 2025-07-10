package com.pixelpals.backend.service;

import com.pixelpals.backend.dto.AuthRequest;
import com.pixelpals.backend.dto.AuthResponse;
import com.pixelpals.backend.dto.RegisterRequest;
import com.pixelpals.backend.model.User;
import lombok.Data;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
// import java.util.UUID; // Non più necessario per la generazione del token

@Service
@Data
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService; // Inietta EmailService

    public AuthService(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService; // Assegna EmailService
    }

    public User register(RegisterRequest request) {
        Optional<User> existingUser = userService.getUserByUsername(request.getUsername());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Username già utilizzato");
        }
        Optional<User> existingEmail = userService.getUserByEmail(request.getEmail());
        if (existingEmail.isPresent()) {
            throw new RuntimeException("Email già registrata");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole("ROLE_USER");
        user.setVerified(false);

        // Genera un token di verifica JWT valido
        String verificationToken = jwtService.generateVerificationToken(user);
        user.setVerificationToken(verificationToken);

        // Estrai la data di scadenza direttamente dal JWT
        user.setTokenExpirationDate(jwtService.extractExpiration(verificationToken));

        User savedUser = userService.saveUser(user);

        // Invia l'email di verifica con il JWT generato
        emailService.sendVerificationEmail(savedUser, verificationToken); // Passa il token

        return savedUser;
    }

    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Recupera l'oggetto User completo per accedere al ruolo
        User authenticatedUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato dopo autenticazione."));

        // Opzionale: Controlla se l'utente è verificato prima di generare il token di login
        if (!authenticatedUser.isVerified()) {
            throw new RuntimeException("Il tuo account non è ancora stato verificato. Controlla la tua email.");
        }

        String accessToken = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(accessToken)
                .username(authenticatedUser.getUsername()) // Includi username
                .role(authenticatedUser.getRole())         // Includi ruolo
                .build();
    }

    public void verifyUserEmail(String token) {
        // Il token passato qui è il JWT
        // Il JwtAuthFilter dovrebbe già aver validato il token e impostato l'autenticazione
        // Tuttavia, per la verifica email, il token non è un token di autenticazione,
        // ma un token di verifica. Quindi, la validazione deve avvenire qui.

        // Estrarre l'email dal token di verifica
        String userEmail = jwtService.extractUsername(token); // extractUsername usa Claims::getSubject

        // Caricare l'utente dal database usando l'email estratta
        User user = userService.getUserByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato per l'email nel token."));

        // Verificare che il token sia valido per questo utente e non sia scaduto
        // La logica di isTokenValid nel JwtService è per i token di autenticazione.
        // Per i token di verifica, dobbiamo fare un controllo manuale qui,
        // o estendere isTokenValid per gestire diversi tipi di token.
        // Per ora, ci affidiamo a JwtService.extractExpiration per la scadenza.
        if (jwtService.isTokenExpired(token)) {
            throw new RuntimeException("Il token di verifica è scaduto.");
        }

        // Chiamare il metodo di verifica nel UserService
        userService.verifyUserEmail(token); // Il UserService.verifyUserEmail ora riceve un JWT valido
    }
}
