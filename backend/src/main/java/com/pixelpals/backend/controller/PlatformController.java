package com.pixelpals.backend.controller;
import com.pixelpals.backend.model.Platform;
import com.pixelpals.backend.service.PlatformService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
@RestController
@RequestMapping("/api/platforms")
@RequiredArgsConstructor
public class PlatformController {
    private final PlatformService platformService;
    @GetMapping
    public List<Platform> getAllPlatforms() {
        return platformService.getAllPlatforms();
    }
}
