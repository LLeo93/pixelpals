package com.pixelpals.backend.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class Platform {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String iconUrl;

    @ManyToMany(mappedBy = "platforms")
    private List<User> users;
}

