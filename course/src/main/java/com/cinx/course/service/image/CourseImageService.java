package com.cinx.course.service.image;

import com.cinx.common.exception.NotFoundException;
import com.cinx.course.dto.request.CreateCourseImageRequest;
import com.cinx.course.dto.request.UpdateCourseImageRequest;
import com.cinx.course.model.Course;
import com.cinx.course.model.CourseImage;
import com.cinx.course.repository.CourseImageRepository;
import com.cinx.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseImageService implements ICourseImageService{
    private final CourseImageRepository courseImageRepository;
    private final CourseRepository courseRepository;

    @Override
    public void saveCourseImages(String courseId, CreateCourseImageRequest request) {  
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        if (request.getImages() != null) {
            for (CreateCourseImageRequest.ImageDto dto : request.getImages()) {
                course.getImages().add(
                        CourseImage.builder()
                                .course(course)
                                .imageUrl(dto.getImageUrl())
                                .publicId(dto.getS3ObjectKey())
                                .build()
                );
            }
        }
        courseImageRepository.saveAll(course.getImages());
    }

    @Override
    public void updateCourseImage(String imageId, UpdateCourseImageRequest request) {        
        courseImageRepository.findById(imageId).ifPresent(image -> {
            image.setImageUrl(request.getImageUrl());
            image.setPublicId(request.getS3ObjectKey());
            courseImageRepository.save(image);
        });
    }

    @Override
    public void deleteImage(String imageId) {
        courseImageRepository.findById(imageId).ifPresent(image -> {
            courseImageRepository.delete(image);
        });
    }
}
