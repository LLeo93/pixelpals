package com.pixelpals.backend;

import com.pixelpals.backend.model.Platform;
import com.pixelpals.backend.repository.PlatformRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlatformSeeder implements CommandLineRunner {
    private final PlatformRepository platformRepository;

    public PlatformSeeder(PlatformRepository platformRepository) {
        this.platformRepository = platformRepository;
    }

    @Override
    public void run(String... args) {
        if (platformRepository.count() == 0) {
            List<Platform> platforms = List.of(
                    new Platform(null, "PC", "https://example.com/icons/pc.png"),
                    new Platform(null, "PlayStation", "https://example.com/icons/ps.png"),
                    new Platform(null, "Xbox", "https://example.com/icons/xbox.png"),
                    new Platform(null, "Switch", "https://example.com/icons/switch.png")
            );
            platformRepository.saveAll(platforms);
        }
    }
}