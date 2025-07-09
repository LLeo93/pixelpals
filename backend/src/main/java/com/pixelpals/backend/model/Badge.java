package com.pixelpals.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document
public class Badge {
    @Id
    private Long id;
    private String name;
    private String iconUrl;
    private String description;
    private boolean automatic;

    private Set<User> users;
}
