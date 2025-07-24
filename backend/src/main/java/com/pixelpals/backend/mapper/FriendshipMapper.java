package com.pixelpals.backend.mapper;
import com.pixelpals.backend.dto.FriendshipDTO;
import com.pixelpals.backend.model.Friendship;
import com.pixelpals.backend.model.User;
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
    public static Friendship toEntity(FriendshipDTO dto, User sender, User receiver) {
        if (dto == null) {
            return null;
        }
        Friendship friendship = new Friendship();
        friendship.setId(dto.getId());
        friendship.setSender(sender);
        friendship.setReceiver(receiver);
        friendship.setStatus(dto.getStatus());
        friendship.setCreatedAt(dto.getCreatedAt());
        friendship.setAcceptedAt(dto.getAcceptedAt());
        return friendship;
    }
}
