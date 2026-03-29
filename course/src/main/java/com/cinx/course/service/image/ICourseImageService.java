package com.cinx.course.service.image;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ICourseImageService {
    void saveCourseImages(String courseId, List<MultipartFile> files);
    void updateCourseImages(String imageId, MultipartFile file);
    void deleteImage(String imageId);
}
