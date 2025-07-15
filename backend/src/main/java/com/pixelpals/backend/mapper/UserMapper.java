package com.pixelpals.backend.mapper;

import com.pixelpals.backend.dto.GameDTO;
import com.pixelpals.backend.dto.PlatformDTO;
import com.pixelpals.backend.dto.UserDTO;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.model.Game; // Assumendo che esista il modello Game
import com.pixelpals.backend.model.Platform; // Assumendo che esista il modello Platform
import com.pixelpals.backend.enumeration.SkillLevel; // Assumendo che esista l'enum SkillLevel

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserMapper {

    // Converte un oggetto User in UserDTO
    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole()); // Mappa il campo role
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setBio(user.getBio());
        dto.setLevel(user.getLevel());
        dto.setRating(user.getRating());
        dto.setOnline(user.isOnline()); // CORREZIONE: usa setOnline() per il DTO (boolean primitivo)
        dto.setVerified(user.isVerified()); // CORREZIONE: usa setVerified() per il DTO (boolean primitivo)

        // Mappa preferredGames (da List<Game> a List<GameDTO>)
        if (user.getPreferredGames() != null) {
            dto.setPreferredGames(user.getPreferredGames().stream()
                    .map(GameMapper::toDTO) // Richiede GameMapper.toDTO()
                    .collect(Collectors.toList()));
        } else {
            dto.setPreferredGames(new ArrayList<>());
        }

        // Mappa platforms (da List<Platform> a List<PlatformDTO>)
        if (user.getPlatforms() != null) {
            dto.setPlatforms(user.getPlatforms().stream()
                    .map(PlatformMapper::toDTO) // Richiede PlatformMapper.toDTO()
                    .collect(Collectors.toList()));
        } else {
            dto.setPlatforms(new ArrayList<>());
        }

        // Mappa skillLevelMap (da Map<String, SkillLevel> a Map<String, String>)
        if (user.getSkillLevelMap() != null) {
            dto.setSkillLevelMap(user.getSkillLevelMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().name()))); // Converte SkillLevel enum in String
        } else {
            dto.setSkillLevelMap(new java.util.HashMap<>());
        }

        return dto;
    }

    // Converte un oggetto UserDTO in User
    public static User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        User user = new User();
        user.setId(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setRole(userDTO.getRole()); // Mappa il campo role
        user.setAvatarUrl(userDTO.getAvatarUrl());
        user.setBio(userDTO.getBio());
        user.setLevel(userDTO.getLevel());
        user.setRating(user.getRating());
        // isOnline e verified sono boolean primitivi nel DTO, quindi non possono essere null.
        // Li mappiamo direttamente.
        user.setOnline(userDTO.isOnline());
        user.setVerified(userDTO.isVerified());

        // Mappa preferredGames (da List<GameDTO> a List<Game>)
        if (userDTO.getPreferredGames() != null) {
            user.setPreferredGames(userDTO.getPreferredGames().stream()
                    .map(GameMapper::toEntity) // Richiede GameMapper.toEntity()
                    .collect(Collectors.toList()));
        } else {
            user.setPreferredGames(new ArrayList<>());
        }

        // Mappa platforms (da List<PlatformDTO> a List<Platform>)
        if (userDTO.getPlatforms() != null) {
            user.setPlatforms(userDTO.getPlatforms().stream()
                    .map(PlatformMapper::toEntity) // Richiede PlatformMapper.toEntity()
                    .collect(Collectors.toList()));
        } else {
            user.setPlatforms(new ArrayList<>());
        }

        // Mappa skillLevelMap (da Map<String, String> a Map<String, SkillLevel>)
        if (userDTO.getSkillLevelMap() != null) {
            user.setSkillLevelMap(userDTO.getSkillLevelMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> SkillLevel.valueOf(e.getValue())))); // Converte String in SkillLevel enum
        } else {
            user.setSkillLevelMap(new java.util.HashMap<>());
        }

        // La password e il token di verifica non dovrebbero essere mappati da DTO a Entity
        // in questo contesto, ma gestiti separatamente (es. durante la registrazione o l'aggiornamento password)
        return user;
    }
}
