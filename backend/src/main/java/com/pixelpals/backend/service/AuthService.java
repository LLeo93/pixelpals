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
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Data
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    // Ora register NON restituisce token, solo conferma
    public void register(RegisterRequest request) {
        Optional<User> existingUser = userService.getUserByUsername(request.getUsername());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Username già utilizzato");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // La password verrà criptata in saveUser
        user.setEmail(request.getEmail());
        user.setRole("ROLE_USER");

        userService.saveUser(user);
    }

    // Login restituisce il token JWT
    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtService.generateToken(userDetails);
        return AuthResponse.builder().token(jwt).build();
    }
}
