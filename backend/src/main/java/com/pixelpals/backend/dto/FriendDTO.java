package com.pixelpals.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendDTO {
    private String id;
    private String username;
    private boolean isOnline;
    private String avatarUrl;
}
