package com.pixelpals.backend.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingRequestDTO {
    private String ratedUserId;
    private int rating;
    private String feedback;
}
