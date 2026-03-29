package com.cinx.course.service.image;

import com.cinx.course.dto.request.CreateCourseImageRequest;
import com.cinx.course.dto.request.UpdateCourseImageRequest;

public interface ICourseImageService {
    void saveCourseImages(String courseId, CreateCourseImageRequest request);
    void updateCourseImage(String imageId, UpdateCourseImageRequest request);
    void deleteImage(String imageId);
}
