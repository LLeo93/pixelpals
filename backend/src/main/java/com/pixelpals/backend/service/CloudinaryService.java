package com.pixelpals.backend.service;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }
    public String uploadFile(MultipartFile file) throws IOException {
        String publicId = "pixelpals_avatars/" + UUID.randomUUID().toString();
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "resource_type", "auto"
                ));
        return uploadResult.get("url").toString();
    }
}