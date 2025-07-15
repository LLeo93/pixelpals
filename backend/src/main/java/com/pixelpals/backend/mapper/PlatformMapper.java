package com.pixelpals.backend.mapper;

import com.pixelpals.backend.dto.PlatformDTO;
import com.pixelpals.backend.model.Platform; // Assicurati che il tuo modello Platform esista

public class PlatformMapper {

    /**
     * Converte un oggetto Platform in PlatformDTO.
     * @param platform L'oggetto Platform da convertire.
     * @return Il PlatformDTO risultante, o null se l'input è null.
     */
    public static PlatformDTO toDTO(Platform platform) {
        if (platform == null) {
            return null;
        }
        PlatformDTO dto = new PlatformDTO();
        dto.setId(platform.getId());
        dto.setName(platform.getName());
        dto.setIconUrl(platform.getIconUrl()); // Mappa il campo iconUrl
        return dto;
    }

    /**
     * Converte un oggetto PlatformDTO in Platform.
     * @param platformDTO L'oggetto PlatformDTO da convertire.
     * @return Il modello Platform risultante, o null se l'input è null.
     */
    public static Platform toEntity(PlatformDTO platformDTO) {
        if (platformDTO == null) {
            return null;
        }
        Platform platform = new Platform();
        platform.setId(platformDTO.getId());
        platform.setName(platformDTO.getName());
        platform.setIconUrl(platformDTO.getIconUrl()); // Mappa il campo iconUrl
        return platform;
    }
}
