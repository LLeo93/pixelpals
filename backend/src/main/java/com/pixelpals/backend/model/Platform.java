package com.pixelpals.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "platforms")
public class Platform {

    @Id
    private String id;

    private String name;
    private String iconUrl;


}
