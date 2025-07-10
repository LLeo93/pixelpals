package com.pixelpals.backend.mapper;
import com.pixelpals.backend.dto.FriendshipDTO;
import com.pixelpals.backend.model.Friendship;
import com.pixelpals.backend.model.User; // Importa User
public class FriendshipMapper {
    public static FriendshipDTO toDTO(Friendship friendship) {
        if (friendship == null) {
            return null;
        }
        FriendshipDTO dto = new FriendshipDTO();
        dto.setId(friendship.getId());
        dto.setSenderId(friendship.getSender().getId());
        dto.setSenderUsername(friendship.getSender().getUsername());
        dto.setReceiverId(friendship.getReceiver().getId());
        dto.setReceiverUsername(friendship.getReceiver().getUsername());
        dto.setStatus(friendship.getStatus());
        dto.setCreatedAt(friendship.getCreatedAt());
        dto.setAcceptedAt(friendship.getAcceptedAt());
        return dto;
    }
    // Metodo per convertire DTO in Entity (utile per creare nuove richieste)
    // Nota: per la conversione da DTO a Entity, avrai bisogno degli oggetti User completi,
    // che dovrai recuperare dal database nel servizio.
    public static Friendship toEntity(FriendshipDTO dto, User sender, User receiver) {
        if (dto == null) {
            return null;
        }
        Friendship friendship = new Friendship();
        friendship.setId(dto.getId()); // L'ID potrebbe essere null per nuove entità
        friendship.setSender(sender);
        friendship.setReceiver(receiver);
        friendship.setStatus(dto.getStatus());
        friendship.setCreatedAt(dto.getCreatedAt());
        friendship.setAcceptedAt(dto.getAcceptedAt());
        return friendship;
    }
}
