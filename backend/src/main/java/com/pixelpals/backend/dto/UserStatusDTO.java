package com.pixelpals.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserStatusDTO {
    private String userId;
    private String username;
    private boolean isOnline;
}
