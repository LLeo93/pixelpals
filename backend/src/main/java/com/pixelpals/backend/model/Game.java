package com.pixelpals.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "games")
public class Game {
    @Id
    private String id;
    private String name;
    private String genre;
    private String imageUrl;


}
