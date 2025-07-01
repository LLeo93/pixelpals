package com.pixelpals.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@Document
public class TimeSlot {

    @Id
    private String id;

    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    // Mongo: embedded o userId come String
    private String userId; // se vuoi riferirlo
}
