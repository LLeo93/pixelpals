package com.pixelpals.backend.mapper;

import com.pixelpals.backend.dto.UserDTO;
import com.pixelpals.backend.model.User;

public class UserMapper {

    public static UserDTO toDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setBio(user.getBio());
        dto.setLevel(user.getLevel());
        dto.setRating(user.getRating());
        dto.setOnline(user.isOnline());
        dto.setAuthProvider(user.getAuthProvider());
        return dto;
    }

    public static User toEntity(UserDTO dto) {
        if (dto == null) return null;
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setBio(dto.getBio());
        user.setLevel(dto.getLevel());
        user.setRating(dto.getRating());
        user.setOnline(dto.isOnline());
        user.setAuthProvider(dto.getAuthProvider());
        return user;
    }
}
