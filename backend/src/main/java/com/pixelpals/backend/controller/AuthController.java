package com.pixelpals.backend.controller;

import com.pixelpals.backend.dto.AuthRequest;
import com.pixelpals.backend.dto.AuthResponse;
import com.pixelpals.backend.dto.RegisterRequest;
import com.pixelpals.backend.dto.UserDTO;
import com.pixelpals.backend.mapper.UserMapper;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.service.AuthService;
import com.pixelpals.backend.service.EmailService;
import com.pixelpals.backend.service.UserService;
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
    // Rimosso l'iniezione di EmailService qui, poiché AuthService lo gestisce internamente
    private final UserService userService;
    // Aggiornato il costruttore
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // AuthService.register ora gestisce l'intera logica di registrazione,
            // inclusa la generazione del token di verifica e l'invio dell'email.
            authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully. Verification email sent.");
        } catch (RuntimeException e) {
            System.err.println("Errore durante la registrazione: " + e.getMessage());
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
    // NUOVO ENDPOINT PER LA VERIFICA EMAIL
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        try {
            authService.verifyUserEmail(token); // Delega la logica al servizio di autenticazione
            return ResponseEntity.ok("Email verificata con successo!");
        } catch (RuntimeException e) {
            System.err.println("Errore verifica email: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
