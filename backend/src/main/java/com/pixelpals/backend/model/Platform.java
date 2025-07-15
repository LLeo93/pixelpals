
package com.pixelpals.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "platforms")
@AllArgsConstructor
@NoArgsConstructor
public class Platform {
    @Id
    private String id;
    private String name;
    private String iconUrl; // Manteniamo 'iconUrl' come da te specificato

    // Costruttore aggiuntivo per facilitare l'inizializzazione nel DataInitializer/DataLoader
    public Platform(String name, String iconUrl) {
        this.name = name;
        this.iconUrl = iconUrl;
    }
}