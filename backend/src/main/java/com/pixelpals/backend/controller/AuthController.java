package com.pixelpals.backend.controller;

import com.pixelpals.backend.dto.AuthRequest;
import com.pixelpals.backend.dto.AuthResponse;
import com.pixelpals.backend.dto.RegisterRequest;
import com.pixelpals.backend.dto.UserDTO;
import com.pixelpals.backend.mapper.UserMapper;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.service.AuthService;
import com.pixelpals.backend.service.EmailService; // Importa EmailService
import com.pixelpals.backend.service.UserService; // Importa UserService
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService; // Dichiarato come final e iniettato
    private final UserService userService; // Dichiarato come final e iniettato

    // Inietta tutti i servizi necessari tramite il costruttore
    public AuthController(AuthService authService, EmailService emailService, UserService userService) {
        this.authService = authService;
        this.emailService = emailService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Cattura l'utente appena registrato dal servizio di autenticazione
            User registeredUser = authService.register(request);

            // Invia l'email di verifica all'utente appena registrato
            // Questo metodo può lanciare un'eccezione se l'invio fallisce
            emailService.sendVerificationEmail(registeredUser);

            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully. Verification email sent.");
        } catch (RuntimeException e) {
            // Se l'emailService.sendVerificationEmail lancia una RuntimeException,
            // questa viene catturata qui.
            // Potresti voler distinguere tra errori di registrazione e errori di invio email.
            System.err.println("Errore durante la registrazione o l'invio dell'email: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        AuthResponse response = authService.login(authRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logout effettuato con successo"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            Optional<User> userOpt = userService.getUserByUsername(username); // Usa userService iniettato
            if (userOpt.isPresent()) {
                UserDTO dto = UserMapper.toDTO(userOpt.get());
                return ResponseEntity.ok(dto);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PutMapping("/me/update")
    public ResponseEntity<?> updateMe(@RequestBody Map<String, Object> updates) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            Optional<User> userOpt = userService.getUserByUsername(username); // Usa userService iniettato

            if (userOpt.isPresent()) {
                User updatedUser = userService.updateUserFields(userOpt.get(), updates); // Usa userService iniettato
                return ResponseEntity.ok(UserMapper.toDTO(updatedUser));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
    }

}
