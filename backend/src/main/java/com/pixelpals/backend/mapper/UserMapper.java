package com.pixelpals.backend.mapper;
import com.pixelpals.backend.dto.GameDTO;
import com.pixelpals.backend.dto.PlatformDTO;
import com.pixelpals.backend.dto.UserDTO;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.model.Game;
import com.pixelpals.backend.model.Platform;
import com.pixelpals.backend.enumeration.SkillLevel;
import com.pixelpals.backend.model.Badge;
import java.util.ArrayList;
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
        dto.setRole(user.getRole());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setBio(user.getBio());
        dto.setLevel(user.getLevel());
        dto.setRating(user.getRating());
        dto.setOnline(user.isOnline());
        dto.setVerified(user.isVerified());
        dto.setMatchesPlayed(user.getMatchesPlayed());
        dto.setNumberOfRatings(user.getNumberOfRatings());
        if (user.getPreferredGames() != null) {
            dto.setPreferredGames(user.getPreferredGames().stream()
                    .map(GameMapper::toDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setPreferredGames(new ArrayList<>());
        }
        if (user.getPlatforms() != null) {
            dto.setPlatforms(user.getPlatforms().stream()
                    .map(PlatformMapper::toDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setPlatforms(new ArrayList<>());
        }
        if (user.getSkillLevelMap() != null) {
            dto.setSkillLevelMap(user.getSkillLevelMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().name())));
        } else {
            dto.setSkillLevelMap(new java.util.HashMap<>());
        }
        dto.setBadges(user.getBadges() != null ? user.getBadges() : Collections.emptyList());
        return dto;
    }
    public static User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        User user = new User();
        user.setId(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setRole(userDTO.getRole());
        user.setAvatarUrl(userDTO.getAvatarUrl());
        user.setBio(userDTO.getBio());
        user.setLevel(userDTO.getLevel());
        user.setRating(userDTO.getRating());
        user.setOnline(userDTO.isOnline());
        user.setVerified(userDTO.isVerified());
        user.setMatchesPlayed(userDTO.getMatchesPlayed());
        user.setNumberOfRatings(userDTO.getNumberOfRatings());
        if (userDTO.getPreferredGames() != null) {
            user.setPreferredGames(userDTO.getPreferredGames().stream()
                    .map(GameMapper::toEntity)
                    .collect(Collectors.toList()));
        } else {
            user.setPreferredGames(new ArrayList<>());
        }
        if (userDTO.getPlatforms() != null) {
            user.setPlatforms(userDTO.getPlatforms().stream()
                    .map(PlatformMapper::toEntity)
                    .collect(Collectors.toList()));
        } else {
            user.setPlatforms(new ArrayList<>());
        }
        if (userDTO.getSkillLevelMap() != null) {
            user.setSkillLevelMap(userDTO.getSkillLevelMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> SkillLevel.valueOf(e.getValue()))));
        } else {
            user.setSkillLevelMap(new java.util.HashMap<>());
        }
        return user;
    }
}
