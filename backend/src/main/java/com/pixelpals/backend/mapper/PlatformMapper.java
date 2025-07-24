package com.pixelpals.backend.mapper;
import com.pixelpals.backend.dto.PlatformDTO;
import com.pixelpals.backend.model.Platform;
public class PlatformMapper {
    public static PlatformDTO toDTO(Platform platform) {
        if (platform == null) {
            return null;
        }
        PlatformDTO dto = new PlatformDTO();
        dto.setId(platform.getId());
        dto.setName(platform.getName());
        dto.setIconUrl(platform.getIconUrl());
        return dto;
    }
    public static Platform toEntity(PlatformDTO platformDTO) {
        if (platformDTO == null) {
            return null;
        }
        Platform platform = new Platform();
        platform.setId(platformDTO.getId());
        platform.setName(platformDTO.getName());
        platform.setIconUrl(platformDTO.getIconUrl());
        return platform;
    }
}
