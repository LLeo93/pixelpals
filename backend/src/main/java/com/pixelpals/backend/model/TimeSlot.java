package com.pixelpals.backend.model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.DayOfWeek;
import java.time.LocalTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlot {
    private DayOfWeek dayOfWeek;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
}
