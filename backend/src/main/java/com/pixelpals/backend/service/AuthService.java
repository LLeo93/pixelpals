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
@Service
@Data
public class AuthService {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    public AuthService(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
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
        String verificationToken = jwtService.generateVerificationToken(user);
        user.setVerificationToken(verificationToken);
        user.setTokenExpirationDate(jwtService.extractExpiration(verificationToken));
        User savedUser = userService.saveUser(user);
        emailService.sendVerificationEmail(savedUser, verificationToken);
        return savedUser;
    }

    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User authenticatedUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato dopo autenticazione."));
        if (!authenticatedUser.isVerified()) {
            throw new RuntimeException("Il tuo account non è ancora stato verificato. Controlla la tua email.");
        }
        userService.setUserOnlineStatus(authenticatedUser.getId(), true);
        String accessToken = jwtService.generateToken(userDetails);
        return AuthResponse.builder()
                .token(accessToken)
                .username(authenticatedUser.getUsername())
                .role(authenticatedUser.getRole())
                .build();
    }
    public void verifyUserEmail(String token) {
        String userEmail = jwtService.extractUsername(token);
        User user = userService.getUserByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato per l'email nel token."));
        if (jwtService.isTokenExpired(token)) {
            throw new RuntimeException("Il token di verifica è scaduto.");
        }
        userService.verifyUserEmail(token);
    }
}
