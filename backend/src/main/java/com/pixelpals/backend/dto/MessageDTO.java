package com.pixelpals.backend.dto;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class MessageDTO {
    private String id;
    private String senderId;
    private String receiverId;
    private String content;
    private LocalDateTime sentAt;
    private String matchId;
}

