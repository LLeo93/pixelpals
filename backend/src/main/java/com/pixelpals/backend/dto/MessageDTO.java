package com.pixelpals.backend.dto;

import lombok.Data;
import java.util.Date;

@Data
public class MessageDTO {
    private String id;
    private String senderId; // ID del mittente
    private String senderUsername; // Username del mittente
    private String receiverId; // ID del destinatario (per chat privata)
    private String receiverUsername; // Username del destinatario
    private String content; // Contenuto del messaggio
    private Date timestamp; // Timestamp del messaggio
    private String chatRoomId; // ID della chat room
    private boolean read;
}
