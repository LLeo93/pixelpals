package com.pixelpals.backend.model;
import jakarta.persistence.*;
import java.util.*;

@Entity
public class Game {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String genre;
    private String imageUrl;

    @ManyToMany(mappedBy = "preferredGames")
    private List<User> users;
}

