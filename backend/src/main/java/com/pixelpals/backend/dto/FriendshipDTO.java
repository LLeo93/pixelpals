package com.pixelpals.backend.dto;
import com.pixelpals.backend.enumeration.FriendshipStatus;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class FriendshipDTO {
    private String id;
    private String senderId;
    private String senderUsername;
    private String receiverId;
    private String receiverUsername;
    private FriendshipStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
}
