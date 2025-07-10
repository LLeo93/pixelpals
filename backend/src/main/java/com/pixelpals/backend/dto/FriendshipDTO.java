package com.pixelpals.backend.dto;
import com.pixelpals.backend.enumeration.FriendshipStatus;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class FriendshipDTO {
    private String id;
    private String senderId;
    private String senderUsername; // Aggiunto per comodità frontend
    private String receiverId;
    private String receiverUsername; // Aggiunto per comodità frontend
    private FriendshipStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
}
