package com.pixelpals.backend.controller;

import com.pixelpals.backend.dto.UserDTO;
import com.pixelpals.backend.mapper.UserMapper;
import com.pixelpals.backend.model.TimeSlot;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.service.CloudinaryService;
import com.pixelpals.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Import aggiunto
import org.springframework.security.core.userdetails.UserDetails; // Import aggiunto
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // Import aggiunto

import java.io.IOException; // Import aggiunto
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    public List<UserDTO> getAllUsers(@RequestParam(required = false) String username) {
        List<User> users;
        if (username != null && !username.trim().isEmpty()) {
            users = userService.searchUsersByUsername(username);
        } else {
            users = userService.getAllUsers();
        }
        return users.stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        var user = UserMapper.toEntity(userDTO);
        var created = userService.createUser(user);
        return new ResponseEntity<>(UserMapper.toDTO(created), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(UserMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // MODIFICA: Inclusione di @AuthenticationPrincipal per controlli di sicurezza
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody Map<String, Object> updates,
                                        @AuthenticationPrincipal UserDetails currentUserDetails) {
        Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User userToUpdate = userOpt.get();

        // Controllo di autorizzazione: solo l'utente stesso o un ADMIN può aggiornare
        if (!currentUserDetails.getUsername().equals(userToUpdate.getUsername()) &&
                !currentUserDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Non autorizzato ad aggiornare questo utente."));
        }

        try {
            User updatedUser = userService.updateUserFields(userToUpdate, updates);
            return new ResponseEntity<>(UserMapper.toDTO(updatedUser), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Errore interno durante l'aggiornamento dell'utente."));
        }
    }

    @PutMapping("/availability")
    public ResponseEntity<?> updateAvailability(Principal principal, @RequestBody List<TimeSlot> timeSlots) {
        userService.updateAvailability(principal.getName(), timeSlots);
        return ResponseEntity.ok(Map.of("message", "Disponibilità aggiornata con successo"));
    }

    @PutMapping("/preferredGames")
    public ResponseEntity<?> updatePreferredGames(Principal principal, @RequestBody List<String> gameNames) {
        boolean success = userService.updatePreferredGames(principal.getName(), gameNames);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Giochi preferiti aggiornati con successo"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Errore nell'aggiornamento dei giochi preferiti"));
        }
    }

    // <-- NUOVO ENDPOINT: updatePlatforms
    @PutMapping("/platforms")
    public ResponseEntity<?> updatePlatforms(Principal principal, @RequestBody List<String> platformNames) {
        boolean success = userService.updatePlatforms(principal.getName(), platformNames);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Piattaforme aggiornate con successo"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Errore nell'aggiornamento delle piattaforme"));
        }
    }
    // NUOVO ENDPOINT: updatePlatforms -->

    @PutMapping("/skillLevels")
    public ResponseEntity<?> updateSkillLevels(Principal principal, @RequestBody Map<String, String> skillLevels) {
        boolean success = userService.updateSkillLevels(principal.getName(), skillLevels);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Skill aggiornate con successo"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Errore nell'aggiornamento delle skill"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id, Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));

        if (!"ROLE_ADMIN".equals(currentUser.getRole()) && !currentUser.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Non autorizzato a cancellare questo utente"));
        }

        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "Utente eliminato con successo"));
    }

    @PostMapping(value = "/me/avatar/upload", consumes = "multipart/form-data") // Endpoint per upload di file
    public ResponseEntity<?> uploadAvatar(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Il file non può essere vuoto."));
        }

        try {
            User updatedUser = userService.uploadAndSetAvatar(userDetails.getUsername(), file);
            return ResponseEntity.ok(UserMapper.toDTO(updatedUser));
        } catch (IOException e) {
            System.err.println("Errore upload Cloudinary: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Errore durante l'upload dell'immagine."));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Errore generico upload: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Errore interno del server."));
        }
    }

    // Endpoint opzionale per impostare l'avatar tramite URL diretto (se vuoi mantenerlo)
    @PutMapping("/me/avatar/url")
    public ResponseEntity<?> updateAvatarByUrl(@AuthenticationPrincipal UserDetails userDetails,
                                               @RequestBody Map<String, String> payload) {
        String avatarUrl = payload.get("avatarUrl");
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "La URL dell'avatar non può essere vuota."));
        }

        try {
            User updatedUser = userService.updateAvatarUrl(userDetails.getUsername(), avatarUrl);
            return ResponseEntity.ok(UserMapper.toDTO(updatedUser));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Errore aggiornamento avatar URL: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Errore interno del server."));
        }
    }
}