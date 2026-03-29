package com.cinx.course.service.image;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CloudinaryImageSaver {
    private final Cloudinary cloudinary;

    @Async("asyncTaskExecutor")
    public void saveImage(MultipartFile file, String publicId) {
        try {
            Map params = ObjectUtils.asMap(
                    "public_id", publicId,
                    "overwrite", true
            );
            cloudinary.uploader().upload(file.getBytes(), params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image");
        }
    }

    @Async("asyncTaskExecutor")
    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image");
        }
    }

    public String generateImageUrl(String publicId) {
        return cloudinary.url().generate(publicId);
    }
}
