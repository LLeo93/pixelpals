package com.pixelpals.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class RefreshToken {
    private String token;
    private String username;
    private Instant expiryDate;
}
