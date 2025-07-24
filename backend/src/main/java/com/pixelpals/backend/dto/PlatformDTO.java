package com.pixelpals.backend.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlatformDTO {
    private String id;
    private String name;
    private String iconUrl;
}
