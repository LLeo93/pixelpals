package com.pixelpals.backend.dto;

import lombok.Data;

@Data
public class GameDTO {
    private String id;
    private String name;
    private String genre;
    private String imageUrl;
}
