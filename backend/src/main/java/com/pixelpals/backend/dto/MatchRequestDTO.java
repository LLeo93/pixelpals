package com.pixelpals.backend.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class MatchRequestDTO {
    private String gameName;
    private String platformName;
    private String skillLevel;
    private List<String> preferredTimeSlots;
    private int maxResults;
}
