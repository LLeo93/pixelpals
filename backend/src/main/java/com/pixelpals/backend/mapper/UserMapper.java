package com.pixelpals.backend.mapper;

import com.pixelpals.backend.dto.GameDTO;
import com.pixelpals.backend.dto.PlatformDTO;
import com.pixelpals.backend.dto.UserDTO;
import com.pixelpals.backend.model.Game;
import com.pixelpals.backend.model.Platform;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.enumeration.SkillLevel; // Importa SkillLevel

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setBio(user.getBio());
        dto.setLevel(user.getLevel());
        dto.setRating(user.getRating());
        dto.setOnline(user.isOnline());
        dto.setVerified(user.isVerified());

        // Mappa preferredGames
        if (user.getPreferredGames() != null) {
            dto.setPreferredGames(user.getPreferredGames().stream()
                    .map(game -> {
                        GameDTO gameDto = new GameDTO();
                        gameDto.setId(game.getId());
                        gameDto.setName(game.getName());
                        gameDto.setGenre(game.getGenre());
                        gameDto.setImageUrl(game.getImageUrl());
                        return gameDto;
                    })
                    .collect(Collectors.toList()));
        } else {
            dto.setPreferredGames(Collections.emptyList());
        }

        // Mappa platforms
        if (user.getPlatforms() != null) {
            dto.setPlatforms(user.getPlatforms().stream()
                    .map(platform -> {
                        PlatformDTO platformDto = new PlatformDTO();
                        platformDto.setId(platform.getId());
                        platformDto.setName(platform.getName());
                        platformDto.setIconUrl(platform.getIconUrl());
                        return platformDto;
                    })
                    .collect(Collectors.toList()));
        } else {
            dto.setPlatforms(Collections.emptyList());
        }

        // Mappa skillLevelMap
        if (user.getSkillLevelMap() != null) {
            dto.setSkillLevelMap(user.getSkillLevelMap().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().name() // Converte l'enum SkillLevel in stringa
                    )));
        } else {
            dto.setSkillLevelMap(Collections.emptyMap());
        }

        return dto;
    }

    public static User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setBio(dto.getBio());
        user.setLevel(dto.getLevel());
        user.setRating(dto.getRating());
        user.setOnline(dto.isOnline());
        user.setVerified(dto.isVerified());

        // Nota: per convertire da DTO a Entity per DBRef (preferredGames, platforms),
        // avresti bisogno di recuperare gli oggetti Game e Platform completi dal repository.
        // Questo mapper non gestisce la logica di recupero DBRef per toEntity,
        // che di solito è responsabilità del servizio.
        // Per skillLevelMap, puoi convertire la stringa in enum.
        if (dto.getSkillLevelMap() != null) {
            user.setSkillLevelMap(dto.getSkillLevelMap().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> SkillLevel.valueOf(entry.getValue().toUpperCase())
                    )));
        }

        return user;
    }
}
