package com.pixelpals.backend.service;
import com.pixelpals.backend.model.Platform;
import com.pixelpals.backend.repository.PlatformRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
@RequiredArgsConstructor
public class PlatformService {
    private final PlatformRepository platformRepository;
    public List<Platform> getAllPlatforms() {
        return platformRepository.findAll();
    }
}

