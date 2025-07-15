// src/main/java/com/pixelpals/backend/model/Game.java
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
    private String genre; // Manteniamo 'genre' come da te specificato
    private String imageUrl; // URL dell'immagine del gioco
    private boolean isFeatured; // NUOVO CAMPO per indicare se il gioco è in primo piano

    // Costruttore aggiuntivo per facilitare l'inizializzazione nel DataInitializer/DataLoader
    // Include tutti i campi che userai per creare nuove istanze
    public Game(String name, String genre, String imageUrl, boolean isFeatured) {
        this.name = name;
        this.genre = genre;
        this.imageUrl = imageUrl;
        this.isFeatured = isFeatured;
    }
}