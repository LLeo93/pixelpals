package com.pixelpals.backend.controller;

import com.pixelpals.backend.dto.AuthRequest;
import com.pixelpals.backend.dto.AuthResponse;
import com.pixelpals.backend.dto.RegisterRequest;
import com.pixelpals.backend.dto.UserDTO;
import com.pixelpals.backend.mapper.UserMapper;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.service.AuthService;
import com.pixelpals.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully. Verification email sent.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        AuthResponse response = authService.login(authRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Principal principal) {
        if (principal != null) {
            try {
                String userId = null;
                if (principal instanceof UsernamePasswordAuthenticationToken) {
                    Object userDetails = ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
                    if (userDetails instanceof User) {
                        userId = ((User) userDetails).getId();
                    } else if (userDetails instanceof UserDetails) {
                        String username = ((UserDetails) userDetails).getUsername();
                        userId = userService.getUserByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found for username: " + username))
                                .getId();
                    }
                }
                if (userId != null) {
                    userService.setUserOnlineStatus(userId, false);
                    SecurityContextHolder.clearContext();
                    return ResponseEntity.ok(Map.of("message", "Logout effettuato con successo"));
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Impossibile determinare l'ID utente per il logout.");
                }
            } catch (UsernameNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utente non trovato durante il logout.");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante il logout.");
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nessun utente autenticato per il logout.");
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            Optional<User> userOpt = userService.getUserByUsername(username);
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
            Optional<User> userOpt = userService.getUserByUsername(username);

            if (userOpt.isPresent()) {
                User updatedUser = userService.updateUserFields(userOpt.get(), updates);
                return ResponseEntity.ok(UserMapper.toDTO(updatedUser));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        try {
            authService.verifyUserEmail(token);
            return ResponseEntity.ok("Email verificata con successo!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/friends/status")
    public ResponseEntity<List<UserDTO>> getFriendsStatus(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            String userId = null;
            if (principal instanceof UsernamePasswordAuthenticationToken) {
                Object userDetails = ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
                if (userDetails instanceof User) {
                    userId = ((User) userDetails).getId();
                } else if (userDetails instanceof UserDetails) {
                    String username = ((UserDetails) userDetails).getUsername();
                    userId = userService.getUserByUsername(username)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found for username: " + username))
                            .getId();
                }
            }

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            List<User> friends = userService.getFriendsWithOnlineStatus(userId);
            List<UserDTO> friendDTOs = friends.stream()
                    .map(UserMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(friendDTOs);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
