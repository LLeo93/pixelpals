package com.pixelpals.backend.dto;
import lombok.Data;

@Data
public class UserDTO {
    private String id;
    private String username;
    private String email;
    private String avatarUrl;
    private String bio;
    private int level;
    private double rating;
    private boolean online;
    private boolean verified;
}
