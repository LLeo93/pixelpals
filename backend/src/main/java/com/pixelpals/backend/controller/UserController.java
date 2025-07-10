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
import java.util.Optional;
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
        // Quando un admin crea un utente, la password nel DTO potrebbe non essere hashata.
        // È responsabilità di AuthService.register hashare la password.
        // Qui, se stiamo creando un utente tramite il UserController,
        // dobbiamo assicurarci che la password sia hashata prima di passarla a userService.createUser.
        // Per semplicità e per evitare duplicazioni di logica di hashing,
        // è spesso meglio che la creazione di nuovi utenti avvenga solo tramite l'endpoint /api/auth/register
        // che gestisce correttamente l'hashing e la verifica.
        // Se questo endpoint POST /api/users è strettamente per un admin che inserisce dati già "pronti",
        // allora la password nel DTO dovrebbe essere già hashata dal frontend o da un servizio intermedio.
        // Per ora, assumiamo che UserMapper.toEntity non hashi la password e che userService.createUser
        // si aspetti una password chiara che *non* verrà hashata di nuovo (come da nostre modifiche recenti).
        // Se vuoi che l'admin possa creare utenti con password in chiaro che vengono hashate,
        // dovrai iniettare PasswordEncoder qui o nel UserService.createUser e hashare lì.
        // Per coerenza, è meglio che l'hashing avvenga sempre in AuthService.
        // Quindi, questo endpoint è più adatto per la creazione di utenti "interni" o pre-hashati.
        var user = UserMapper.toEntity(userDTO);
        var created = userService.createUser(user); // Questo createUser non hasha la password
        return new ResponseEntity<>(UserMapper.toDTO(created), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(UserMapper.toDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    // NUOVO ENDPOINT: AGGIORNAMENTO UTENTE TRAMITE ID (per Admin Dashboard)
    // Questo endpoint permette all'admin di aggiornare qualsiasi campo di un utente specifico.
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            User updatedUser = userService.updateUserFields(userOpt.get(), updates);
            return ResponseEntity.ok(UserMapper.toDTO(updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Errore interno durante l'aggiornamento dell'utente."));
        }
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
