package com.pixelpals.backend.controller;

import com.pixelpals.backend.dto.UserDTO;
import com.pixelpals.backend.mapper.UserMapper;
import com.pixelpals.backend.model.TimeSlot;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers()
                .stream()
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
                .map(user -> ResponseEntity.ok(UserMapper.toDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    // AGGIORNAMENTO DISPONIBILITÀ (TimeSlots)
    @PutMapping("/availability")
    public ResponseEntity<?> updateAvailability(Principal principal, @RequestBody List<TimeSlot> timeSlots) {
        userService.updateAvailability(principal.getName(), timeSlots);
        return ResponseEntity.ok(Map.of("message", "Disponibilità aggiornata con successo"));
    }

    // AGGIORNAMENTO GIOCHI PREFERITI (lista di nomi giochi)
    @PutMapping("/preferredGames")
    public ResponseEntity<?> updatePreferredGames(Principal principal, @RequestBody List<String> gameNames) {
        boolean success = userService.updatePreferredGames(principal.getName(), gameNames);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Giochi preferiti aggiornati con successo"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Errore nell'aggiornamento dei giochi preferiti"));
        }
    }

    // AGGIORNAMENTO SKILL LEVEL MAP (mappa gioco->skill stringhe)
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
}
