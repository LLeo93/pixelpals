package com.pixelpals.backend.model;
import com.pixelpals.backend.enumeration.FriendshipStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
@Document(collection = "friendships")
@Data
public class Friendship {
    @Id
    private String id;
    @DBRef
    private User sender;
    @DBRef
    private User receiver;
    private FriendshipStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
}

