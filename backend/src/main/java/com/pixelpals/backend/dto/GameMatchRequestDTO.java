package com.pixelpals.backend.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameMatchRequestDTO {
    private String receiverId;
    private String gameId;
}
