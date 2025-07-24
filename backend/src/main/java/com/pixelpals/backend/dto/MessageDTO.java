package com.pixelpals.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {
    private String id;
    private String senderId;
    private String senderUsername;
    private String receiverId;
    private String receiverUsername;
    private String content;
    private LocalDateTime timestamp;
    private boolean read;
    private String chatRoomId;
}
